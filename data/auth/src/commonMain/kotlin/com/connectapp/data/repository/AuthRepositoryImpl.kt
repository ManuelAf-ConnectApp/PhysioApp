package com.connectapp.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.connectapp.core.crypto.Pbkdf2PasswordHasher
import com.connectapp.core.crypto.secureRandomBytes
import com.connectapp.core.crypto.toHex
import com.connectapp.core.database.physio.PhysioDatabase
import com.connectapp.data.datasource.SessionStore
import com.connectapp.domain.model.Account
import com.connectapp.domain.model.AuthError
import com.connectapp.domain.model.ProfessionalProfile
import com.connectapp.domain.policy.EpochClock
import com.connectapp.domain.policy.LoginLockoutPolicy
import com.connectapp.domain.repository.AuthRepository
import com.connectapp.domain.shared.professional.ProfessionalCollege
import com.connectapp.domain.shared.security.RecoveryCode
import com.connectapp.domain.shared.text.SearchText
import com.connectapp.domain.shared.validation.EmailValidator
import com.connectapp.domain.shared.validation.LicenseNumberValidator
import com.connectapp.domain.shared.validation.SpanishIdValidator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import com.connectapp.core.database.Account as AccountRow
import com.connectapp.core.database.Professional as ProfessionalRow

// One implementation of the whole AuthRepository contract (see the note there).
/**
 * Local authentication over the encrypted [PhysioDatabase] (docs/00-auditoria-y-plan.md §14,
 * phase 4).
 *
 * - Passwords and recovery codes are stored only as PBKDF2 hashes ([hasher], D16).
 * - Unknown emails cost the same key derivation as known ones (verification against a dummy hash)
 *   and go through the same lockout, kept in memory for them, so neither the message nor the
 *   timing nor the lockout reveals whether an email is registered.
 * - The session lives in memory; "remember session" also writes it to [sessionStore].
 *
 * Key derivation is CPU-bound, so every operation that hashes runs on [dispatcher].
 */
@Suppress("TooManyFunctions")
@OptIn(ExperimentalCoroutinesApi::class)
class AuthRepositoryImpl(
    private val database: PhysioDatabase,
    private val sessionStore: SessionStore,
    private val hasher: Pbkdf2PasswordHasher,
    private val clock: EpochClock,
    private val randomBytes: (Int) -> ByteArray = ::secureRandomBytes,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : AuthRepository {

    private val accounts = database.accountQueries
    private val professionals = database.professionalQueries

    private class Session(val accountId: String, val remembered: Boolean)

    private val session = MutableStateFlow<Session?>(null)
    private val lastActivity = MutableStateFlow<Long?>(null)
    private var lastPersistedActivity = 0L

    private class Throttle(val failedAttempts: Long, val lockedUntil: Long?)

    private val unknownEmailThrottle = mutableMapOf<String, Throttle>()

    // A real PBKDF2 hash with the current parameters, so verifying against it costs the same.
    private val dummyHash: String by lazy { hasher.hash(toHex(randomBytes(DUMMY_SECRET_BYTES))) }

    override suspend fun register(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
    ): Result<String> = authOperation {
        val normalizedEmail = EmailValidator.normalize(email)
        if (accounts.selectAccountByEmail(normalizedEmail).executeAsOneOrNull() != null) {
            throw AuthError.EmailAlreadyRegistered
        }
        val recoveryCode = newRecoveryCode()
        val now = clock.nowMillis()
        runCatching {
            accounts.insertAccount(
                id = randomUuid(),
                first_name = firstName,
                last_name = lastName,
                email_normalized = normalizedEmail,
                password_hash = hasher.hash(password),
                recovery_code_hash = hasher.hash(recoveryCode),
                professional_id = null,
                created_at = now,
                updated_at = now,
            )
        }.onFailure { error ->
            // Lost a race against another registration of the same email (UNIQUE constraint).
            if (accounts.selectAccountByEmail(normalizedEmail).executeAsOneOrNull() != null) {
                throw AuthError.EmailAlreadyRegistered
            }
            throw error
        }
        RecoveryCode.format(recoveryCode)
    }

    override suspend fun login(email: String, password: String, rememberSession: Boolean): Result<Account> =
        authOperation {
            val normalizedEmail = EmailValidator.normalize(email)
            val now = clock.nowMillis()
            val row = accounts.selectAccountByEmail(normalizedEmail).executeAsOneOrNull()
                ?: failUnknownEmailLogin(normalizedEmail, password, now)
            row.locked_until?.takeIf { it > now }?.let { throw AuthError.AccountLocked(it) }
            if (!hasher.verify(password, row.password_hash)) {
                val next = nextThrottle(row.failed_attempts, now)
                accounts.updateLoginFailures(next.failedAttempts, next.lockedUntil, now, row.id)
                throw next.toError()
            }
            accounts.clearLoginFailures(now, row.id)
            if (hasher.needsRehash(row.password_hash)) {
                accounts.updatePasswordHash(hasher.hash(password), now, row.id)
            }
            startSession(row.id, rememberSession, now)
            row.toDomain()
        }

    override suspend fun resetPassword(email: String, recoveryCode: String, newPassword: String): Result<String> =
        authOperation {
            val row = accounts.selectAccountByEmail(EmailValidator.normalize(email)).executeAsOneOrNull()
            val code = RecoveryCode.normalize(recoveryCode)
            val storedHash = row?.recovery_code_hash
            val matches = hasher.verify(code, storedHash ?: dummyHash) && storedHash != null
            if (row == null || !matches) throw AuthError.InvalidRecoveryData
            val newCode = newRecoveryCode()
            accounts.resetCredentials(
                password_hash = hasher.hash(newPassword),
                recovery_code_hash = hasher.hash(newCode),
                updated_at = clock.nowMillis(),
                id = row.id,
            )
            if (sessionStore.accountId() == row.id) sessionStore.clear()
            RecoveryCode.format(newCode)
        }

    override suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> =
        authOperation {
            val row = verifySessionPassword(currentPassword)
            accounts.changePassword(hasher.hash(newPassword), clock.nowMillis(), row.id)
        }

    override suspend fun regenerateRecoveryCode(password: String): Result<String> = authOperation {
        val row = verifySessionPassword(password)
        val newCode = newRecoveryCode()
        accounts.updateRecoveryCodeHash(hasher.hash(newCode), clock.nowMillis(), row.id)
        RecoveryCode.format(newCode)
    }

    override suspend fun verifyPassword(password: String): Result<Unit> = authOperation {
        verifySessionPassword(password)
        Unit
    }

    override suspend fun hasAccounts(): Boolean = withContext(dispatcher) {
        accounts.countAccounts().executeAsOne() > 0
    }

    override suspend fun completeProfessionalProfile(
        nif: String,
        college: ProfessionalCollege,
        licenseNumber: String,
        firstName: String,
        lastName: String,
    ): Result<Account> = authOperation {
        val accountId = session.value?.accountId ?: throw AuthError.NotAuthenticated
        val current = accounts.selectAccountById(accountId).executeAsOneOrNull() ?: throw AuthError.NotAuthenticated
        if (current.professional_id != null) return@authOperation current.toDomain()

        val normalizedNif = SpanishIdValidator.normalize(nif)
        val licenseKey = LicenseNumberValidator.normalize(licenseNumber)
        val now = clock.nowMillis()
        linkableProfessionalId(normalizedNif, college, licenseKey)?.let { existingId ->
            check(accounts.linkProfessional(existingId, now, accountId).value == 1L) {
                "The account was linked concurrently"
            }
            return@authOperation accounts.selectAccountById(accountId).executeAsOne().toDomain()
        }
        ensureProfessionalIsNew(normalizedNif, college, licenseKey)
        val professionalId = randomUuid()
        runCatching {
            database.transaction {
                professionals.insertProfessional(
                    id = professionalId,
                    nif = normalizedNif,
                    college_id = college.name,
                    license_number = licenseNumber.trim(),
                    license_number_key = licenseKey,
                    first_name = firstName,
                    last_name = lastName,
                    full_name_search = SearchText.fullName(firstName, lastName),
                    address = null,
                    email = null,
                    phone = null,
                    notes = null,
                    created_at = now,
                    updated_at = now,
                )
                check(accounts.linkProfessional(professionalId, now, accountId).value == 1L) {
                    "The account was linked concurrently"
                }
            }
        }.onFailure { error ->
            // A concurrent insert may have taken the NIF or license number (UNIQUE constraints).
            ensureProfessionalIsNew(normalizedNif, college, licenseKey)
            throw error
        }
        accounts.selectAccountById(accountId).executeAsOne().toDomain()
    }

    override suspend fun restoreSession(): Account? = withContext(dispatcher) {
        val accountId = sessionStore.accountId()
        val storedActivity = sessionStore.lastActivityAt()
        val row = accountId?.let { accounts.selectAccountById(it).executeAsOneOrNull() }
        if (row == null || storedActivity == null) {
            sessionStore.clear()
            null
        } else {
            session.value = Session(row.id, remembered = true)
            lastActivity.value = storedActivity
            lastPersistedActivity = storedActivity
            row.toDomain()
        }
    }

    override fun observeCurrentAccount(): Flow<Account?> = session.flatMapLatest { current ->
        if (current == null) {
            flowOf(null)
        } else {
            accounts.selectAccountById(current.accountId).asFlow().mapToOneOrNull(dispatcher).map { it?.toDomain() }
        }
    }

    override fun observeCurrentProfessional(): Flow<ProfessionalProfile?> =
        observeCurrentAccount().flatMapLatest { account ->
            val professionalId = account?.professionalId
            if (professionalId == null) {
                flowOf(null)
            } else {
                professionals.selectProfessionalById(professionalId).asFlow().mapToOneOrNull(dispatcher)
                    .map { it?.toDomain() }
            }
        }

    override fun recordActivity() {
        val current = session.value ?: return
        val now = clock.nowMillis()
        lastActivity.value = now
        // Throttled: a write to the OS keystore on every touch would be wasteful.
        if (current.remembered && now - lastPersistedActivity >= ACTIVITY_PERSIST_INTERVAL_MILLIS) {
            sessionStore.saveLastActivity(now)
            lastPersistedActivity = now
        }
    }

    override fun lastActivityAt(): Long? = if (session.value == null) null else lastActivity.value

    override fun logout() {
        session.value = null
        lastActivity.value = null
        sessionStore.clear()
    }

    private fun startSession(accountId: String, remember: Boolean, now: Long) {
        session.value = Session(accountId, remember)
        lastActivity.value = now
        lastPersistedActivity = now
        if (remember) sessionStore.save(accountId, now) else sessionStore.clear()
    }

    /**
     * Re-checks the password of the signed-in account before a sensitive change (D15). Failures
     * share the login counter; on reaching the lockout the session ends as well, since whoever is
     * guessing may be at an unattended, signed-in tablet.
     */
    private fun verifySessionPassword(password: String): AccountRow {
        val row = session.value?.let { accounts.selectAccountById(it.accountId).executeAsOneOrNull() }
            ?: throw AuthError.NotAuthenticated
        val now = clock.nowMillis()
        if (!hasher.verify(password, row.password_hash)) {
            val next = nextThrottle(row.failed_attempts, now)
            accounts.updateLoginFailures(next.failedAttempts, next.lockedUntil, now, row.id)
            if (next.lockedUntil != null) logout()
            throw next.toError()
        }
        if (row.failed_attempts > 0) accounts.clearLoginFailures(now, row.id)
        return row
    }

    private fun failUnknownEmailLogin(email: String, password: String, now: Long): Nothing {
        unknownEmailThrottle[email]?.lockedUntil?.takeIf { it > now }?.let { throw AuthError.AccountLocked(it) }
        hasher.verify(password, dummyHash)
        val next = nextThrottle(unknownEmailThrottle[email]?.failedAttempts ?: 0, now)
        if (unknownEmailThrottle.size >= MAX_TRACKED_UNKNOWN_EMAILS) unknownEmailThrottle.clear()
        unknownEmailThrottle[email] = next
        throw next.toError()
    }

    private fun nextThrottle(failedAttempts: Long, now: Long): Throttle {
        val attempts = failedAttempts + 1
        return if (attempts >= LoginLockoutPolicy.MAX_FAILED_ATTEMPTS) {
            Throttle(failedAttempts = 0, lockedUntil = now + LoginLockoutPolicy.LOCKOUT_MILLIS)
        } else {
            Throttle(failedAttempts = attempts, lockedUntil = null)
        }
    }

    private fun Throttle.toError(): AuthError =
        lockedUntil?.let { AuthError.AccountLocked(it) } ?: AuthError.InvalidCredentials

    /**
     * D5: a record created from "Registrar profesional" before its owner had an account is linked
     * when NIF, college and licence number all match and no other account uses it.
     */
    private fun linkableProfessionalId(nif: String, college: ProfessionalCollege, licenseKey: String): String? {
        val existing = professionals.selectProfessionalByNif(nif).executeAsOneOrNull() ?: return null
        val matches = existing.college_id == college.name && existing.license_number_key == licenseKey
        val free = professionals.countLinkedAccounts(existing.id).executeAsOne() == 0L
        return existing.id.takeIf { matches && free && existing.archived_at == null }
    }

    private fun ensureProfessionalIsNew(nif: String, college: ProfessionalCollege, licenseKey: String) {
        if (professionals.selectProfessionalByNif(nif).executeAsOneOrNull() != null) {
            throw AuthError.NifAlreadyRegistered
        }
        if (professionals.selectProfessionalIdByLicense(college.name, licenseKey).executeAsOneOrNull() != null) {
            throw AuthError.LicenseNumberAlreadyRegistered
        }
    }

    private fun newRecoveryCode(): String = RecoveryCode.fromRandomBytes(randomBytes(RecoveryCode.LENGTH))

    /** Runs [block] on [dispatcher]; [AuthError]s become failures, anything else [AuthError.Unknown]. */
    private suspend fun <T> authOperation(block: suspend () -> T): Result<T> = withContext(dispatcher) {
        try {
            Result.success(block())
        } catch (error: CancellationException) {
            throw error
        } catch (error: AuthError) {
            Result.failure(error)
        } catch (@Suppress("TooGenericExceptionCaught") error: Exception) {
            Result.failure(AuthError.Unknown(error))
        }
    }

    private companion object {
        const val DUMMY_SECRET_BYTES = 16
        const val ACTIVITY_PERSIST_INTERVAL_MILLIS = 15_000L
        const val MAX_TRACKED_UNKNOWN_EMAILS = 1_000
    }
}

private fun AccountRow.toDomain() = Account(
    id = id,
    firstName = first_name,
    lastName = last_name,
    email = email_normalized,
    professionalId = professional_id,
)

private fun ProfessionalRow.toDomain() = ProfessionalProfile(
    id = id,
    nif = nif,
    college = checkNotNull(ProfessionalCollege.fromId(college_id)) { "Unknown college id" },
    licenseNumber = license_number,
    firstName = first_name,
    lastName = last_name,
)

@OptIn(ExperimentalUuidApi::class)
private fun randomUuid(): String = Uuid.random().toString()
