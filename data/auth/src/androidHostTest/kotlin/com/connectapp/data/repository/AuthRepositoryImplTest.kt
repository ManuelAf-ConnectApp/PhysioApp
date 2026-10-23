package com.connectapp.data.repository

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.connectapp.core.crypto.Pbkdf2PasswordHasher
import com.connectapp.core.database.physio.PhysioDatabase
import com.connectapp.data.datasource.SessionStore
import com.connectapp.domain.model.AuthError
import com.connectapp.domain.policy.EpochClock
import com.connectapp.domain.policy.LoginLockoutPolicy
import com.connectapp.domain.shared.professional.ProfessionalCollege
import com.connectapp.domain.shared.professional.ProfessionalCollege.MADRID
import com.connectapp.domain.shared.security.RecoveryCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import java.util.Properties
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Local authentication against the real PhysioDatabase schema (docs/00-auditoria-y-plan.md §9),
 * on plain SQLite through JDBC with foreign keys on. PBKDF2 runs with few iterations to keep the
 * suite fast; the production parameters are covered by :core:crypto.
 */
class AuthRepositoryImplTest {

    private val driver = JdbcSqliteDriver(
        JdbcSqliteDriver.IN_MEMORY,
        Properties().apply { put("foreign_keys", "true") },
    ).also { PhysioDatabase.Schema.create(it) }

    private val database = PhysioDatabase(driver)
    private var now = 1_700_000_000_000L
    private val sessionStore = InMemorySessionStore()
    private val random = Random(seed = 7)

    private fun repository(hasher: Pbkdf2PasswordHasher = FAST_HASHER) = AuthRepositoryImpl(
        database = database,
        sessionStore = sessionStore,
        hasher = hasher,
        clock = EpochClock { now },
        randomBytes = { size -> random.nextBytes(size) },
        dispatcher = Dispatchers.Unconfined,
    )

    private val repository = repository()

    @AfterTest
    fun tearDown() = driver.close()

    private suspend fun registerAina(email: String = "Ana@Clinica.es"): String =
        repository.register("Aina", "García", email, PASSWORD).getOrThrow()

    private fun storedAccount() = database.accountQueries.selectAccountByEmail("ana@clinica.es").executeAsOne()

    // ---- Register ----

    @Test
    fun `register stores hashes only and returns a well-formed recovery code`() = runTest {
        val code = registerAina()

        val row = storedAccount()
        assertTrue(RecoveryCode.isWellFormed(code))
        assertEquals("ana@clinica.es", row.email_normalized)
        assertFalse(row.password_hash.contains(PASSWORD))
        assertTrue(row.password_hash.startsWith("pbkdf2_sha256$"))
        assertTrue(FAST_HASHER.verify(PASSWORD, row.password_hash))
        assertTrue(FAST_HASHER.verify(RecoveryCode.normalize(code), assertNotNull(row.recovery_code_hash)))
        assertFalse(assertNotNull(row.recovery_code_hash).contains(RecoveryCode.normalize(code)))
        assertNull(row.professional_id)
    }

    @Test
    fun `register does not sign in`() = runTest {
        registerAina()

        assertNull(repository.observeCurrentAccount().first())
        assertNull(repository.lastActivityAt())
    }

    // "Dado que existe Ana@Clinica.es, cuando intento registrar ana@clinica.es, se rechaza".
    @Test
    fun `an email differing only in case is rejected as duplicate`() = runTest {
        registerAina("Ana@Clinica.es")

        val result = repository.register("Otra", "Persona", " ana@clinica.es ", PASSWORD)

        assertEquals(AuthError.EmailAlreadyRegistered, result.exceptionOrNull())
        assertEquals(1L, database.accountQueries.countAccounts().executeAsOne())
    }

    // ---- Login ----

    @Test
    fun `login with valid credentials starts a session`() = runTest {
        registerAina()

        val account = repository.login("ANA@clinica.es", PASSWORD, rememberSession = false).getOrThrow()

        assertEquals("ana@clinica.es", account.email)
        assertFalse(account.hasProfessionalProfile)
        assertEquals(account, repository.observeCurrentAccount().first())
        assertEquals(account, repository.observeCurrentAccount().first())
        assertEquals(now, repository.lastActivityAt())
    }

    // A wrong password and an unknown email show the same generic error.
    @Test
    fun `a wrong password and an unknown email fail with the same error`() = runTest {
        registerAina()

        val wrongPassword = repository.login("ana@clinica.es", "Otra-Clave-2026", false).exceptionOrNull()
        val unknownEmail = repository.login("nadie@clinica.es", PASSWORD, false).exceptionOrNull()

        assertEquals(AuthError.InvalidCredentials, wrongPassword)
        assertEquals(AuthError.InvalidCredentials, unknownEmail)
        assertNull(repository.observeCurrentAccount().first())
    }

    @Test
    fun `consecutive failures lock the account and a correct password is refused meanwhile`() = runTest {
        registerAina()
        repeat(LoginLockoutPolicy.MAX_FAILED_ATTEMPTS - 1) {
            assertEquals(AuthError.InvalidCredentials, repository.login("ana@clinica.es", "Mala-Clave-2026", false).exceptionOrNull())
        }

        val locking = repository.login("ana@clinica.es", "Mala-Clave-2026", false).exceptionOrNull()
        val whileLocked = repository.login("ana@clinica.es", PASSWORD, false).exceptionOrNull()

        val lockedUntil = now + LoginLockoutPolicy.LOCKOUT_MILLIS
        assertEquals(AuthError.AccountLocked(lockedUntil), locking)
        assertEquals(AuthError.AccountLocked(lockedUntil), whileLocked)
        assertEquals(lockedUntil, storedAccount().locked_until)
    }

    @Test
    fun `the lock expires and a successful login clears the failure counter`() = runTest {
        registerAina()
        repeat(LoginLockoutPolicy.MAX_FAILED_ATTEMPTS) { repository.login("ana@clinica.es", "Mala-Clave-2026", false) }

        now += LoginLockoutPolicy.LOCKOUT_MILLIS + 1
        repository.login("ana@clinica.es", "Mala-Clave-2026", false)
        assertTrue(repository.login("ana@clinica.es", PASSWORD, false).isSuccess)

        assertEquals(0L, storedAccount().failed_attempts)
        assertNull(storedAccount().locked_until)
    }

    // Locking only real accounts would reveal which emails exist.
    @Test
    fun `unknown emails are locked exactly like real ones`() = runTest {
        repeat(LoginLockoutPolicy.MAX_FAILED_ATTEMPTS - 1) {
            assertEquals(AuthError.InvalidCredentials, repository.login("nadie@clinica.es", PASSWORD, false).exceptionOrNull())
        }

        val error = repository.login("nadie@clinica.es", PASSWORD, false).exceptionOrNull()

        assertEquals(AuthError.AccountLocked(now + LoginLockoutPolicy.LOCKOUT_MILLIS), error)
    }

    @Test
    fun `a hash with old parameters is upgraded on successful login`() = runTest {
        repository(Pbkdf2PasswordHasher(iterations = 500)).register("Aina", "García", "ana@clinica.es", PASSWORD)

        repository.login("ana@clinica.es", PASSWORD, false).getOrThrow()

        assertTrue(storedAccount().password_hash.startsWith("pbkdf2_sha256$1000$"))
    }

    // ---- Session ----

    @Test
    fun `a remembered session is stored and restored after a restart`() = runTest {
        registerAina()
        val account = repository.login("ana@clinica.es", PASSWORD, rememberSession = true).getOrThrow()

        val restarted = repository()

        assertEquals(account.id, sessionStore.accountId)
        assertEquals(account, restarted.restoreSession())
        assertEquals(now, restarted.lastActivityAt())
    }

    @Test
    fun `a session that is not remembered is not stored`() = runTest {
        registerAina()
        repository.login("ana@clinica.es", PASSWORD, rememberSession = false).getOrThrow()

        assertNull(sessionStore.accountId)
        assertNull(repository().restoreSession())
    }

    @Test
    fun `a stored session whose account no longer exists is discarded`() = runTest {
        sessionStore.save("deleted", now)

        assertNull(repository.restoreSession())
        assertNull(sessionStore.accountId)
    }

    @Test
    fun `activity is recorded and persisted at most every 15 seconds`() = runTest {
        registerAina()
        repository.login("ana@clinica.es", PASSWORD, rememberSession = true).getOrThrow()
        val loginTime = now

        now += 5_000
        repository.recordActivity()
        assertEquals(now, repository.lastActivityAt())
        assertEquals(loginTime, sessionStore.lastActivityAt)

        now += 15_000
        repository.recordActivity()
        assertEquals(now, sessionStore.lastActivityAt)
    }

    @Test
    fun `logout ends the session and forgets the remembered one`() = runTest {
        registerAina()
        repository.login("ana@clinica.es", PASSWORD, rememberSession = true).getOrThrow()

        repository.logout()
        repository.recordActivity()

        assertNull(repository.observeCurrentAccount().first())
        assertNull(repository.lastActivityAt())
        assertNull(repository.observeCurrentAccount().first())
        assertNull(sessionStore.accountId)
    }

    // ---- Password reset (D4) ----

    @Test
    fun `reset with the right code changes the password and issues a new single-use code`() = runTest {
        val firstCode = registerAina()

        val secondCode = repository.resetPassword("ana@clinica.es", firstCode.lowercase(), NEW_PASSWORD).getOrThrow()

        assertNotEquals(firstCode, secondCode)
        assertTrue(RecoveryCode.isWellFormed(secondCode))
        assertEquals(AuthError.InvalidCredentials, repository.login("ana@clinica.es", PASSWORD, false).exceptionOrNull())
        assertTrue(repository.login("ana@clinica.es", NEW_PASSWORD, false).isSuccess)
        // The used code no longer works; the new one does.
        assertEquals(
            AuthError.InvalidRecoveryData,
            repository.resetPassword("ana@clinica.es", firstCode, PASSWORD).exceptionOrNull(),
        )
        assertTrue(repository.resetPassword("ana@clinica.es", secondCode, PASSWORD).isSuccess)
    }

    // Knowing only the email is not enough, and the error does not reveal whether it exists.
    @Test
    fun `reset with a wrong code or an unknown email fails with the same neutral error`() = runTest {
        registerAina()
        val wrongCode = "22222-22222-22222-22222"

        val known = repository.resetPassword("ana@clinica.es", wrongCode, NEW_PASSWORD).exceptionOrNull()
        val unknown = repository.resetPassword("nadie@clinica.es", wrongCode, NEW_PASSWORD).exceptionOrNull()

        assertEquals(AuthError.InvalidRecoveryData, known)
        assertEquals(AuthError.InvalidRecoveryData, unknown)
        assertTrue(FAST_HASHER.verify(PASSWORD, storedAccount().password_hash))
    }

    @Test
    fun `reset unlocks a locked account`() = runTest {
        val code = registerAina()
        repeat(LoginLockoutPolicy.MAX_FAILED_ATTEMPTS) { repository.login("ana@clinica.es", "Mala-Clave-2026", false) }

        repository.resetPassword("ana@clinica.es", code, NEW_PASSWORD).getOrThrow()

        assertTrue(repository.login("ana@clinica.es", NEW_PASSWORD, false).isSuccess)
    }

    // ---- Settings: change password and regenerate recovery code (D15) ----

    @Test
    fun `changing the password needs the current one and keeps the session`() = runTest {
        registerAina()
        repository.login("ana@clinica.es", PASSWORD, false).getOrThrow()

        assertEquals(AuthError.InvalidCredentials, repository.changePassword("Mala-Clave-2026", NEW_PASSWORD).exceptionOrNull())
        repository.changePassword(PASSWORD, NEW_PASSWORD).getOrThrow()

        assertEquals(0L, storedAccount().failed_attempts)
        assertNotNull(repository.observeCurrentAccount().first())
        repository.logout()
        assertEquals(AuthError.InvalidCredentials, repository.login("ana@clinica.es", PASSWORD, false).exceptionOrNull())
        assertTrue(repository.login("ana@clinica.es", NEW_PASSWORD, false).isSuccess)
    }

    @Test
    fun `regenerating the recovery code invalidates the previous one`() = runTest {
        val firstCode = registerAina()
        repository.login("ana@clinica.es", PASSWORD, false).getOrThrow()

        val secondCode = repository.regenerateRecoveryCode(PASSWORD).getOrThrow()

        assertNotEquals(firstCode, secondCode)
        assertTrue(RecoveryCode.isWellFormed(secondCode))
        assertTrue(FAST_HASHER.verify(PASSWORD, storedAccount().password_hash))
        assertEquals(
            AuthError.InvalidRecoveryData,
            repository.resetPassword("ana@clinica.es", firstCode, NEW_PASSWORD).exceptionOrNull(),
        )
        assertTrue(repository.resetPassword("ana@clinica.es", secondCode, NEW_PASSWORD).isSuccess)
    }

    @Test
    fun `wrong passwords in settings lock the account and end the session`() = runTest {
        val code = registerAina()
        repository.login("ana@clinica.es", PASSWORD, rememberSession = true).getOrThrow()

        repeat(LoginLockoutPolicy.MAX_FAILED_ATTEMPTS - 1) {
            assertEquals(AuthError.InvalidCredentials, repository.regenerateRecoveryCode("Mala-Clave-2026").exceptionOrNull())
        }
        val last = repository.changePassword("Mala-Clave-2026", NEW_PASSWORD).exceptionOrNull()

        assertTrue(last is AuthError.AccountLocked)
        assertNull(repository.observeCurrentAccount().first())
        assertNull(sessionStore.accountId)
        assertTrue(repository.login("ana@clinica.es", PASSWORD, false).exceptionOrNull() is AuthError.AccountLocked)
        // The recovery code was not replaced by any of the failed attempts.
        assertTrue(repository.resetPassword("ana@clinica.es", code, NEW_PASSWORD).isSuccess)
    }

    @Test
    fun `settings operations require a session`() = runTest {
        registerAina()

        assertEquals(AuthError.NotAuthenticated, repository.changePassword(PASSWORD, NEW_PASSWORD).exceptionOrNull())
        assertEquals(AuthError.NotAuthenticated, repository.regenerateRecoveryCode(PASSWORD).exceptionOrNull())
    }

    // ---- Professional profile (D3) ----

    @Test
    fun `completing the profile creates the professional and links it to the account`() = runTest {
        registerAina()
        repository.login("ana@clinica.es", PASSWORD, false).getOrThrow()

        val account = repository.completeProfessionalProfile("12345678-z", MADRID, "28/1234", "Aina", "García López").getOrThrow()

        assertTrue(account.hasProfessionalProfile)
        val professional = assertNotNull(repository.observeCurrentProfessional().first())
        assertEquals("12345678Z", professional.nif)
        assertEquals("28/1234", professional.licenseNumber)
        val row = database.professionalQueries.selectProfessionalById(professional.id).executeAsOne()
        assertEquals("aina garcia lopez", row.full_name_search)
    }

    @Test
    fun `completing the profile requires a session`() = runTest {
        val error = repository.completeProfessionalProfile("12345678Z", MADRID, "28/1234", "Aina", "García").exceptionOrNull()

        assertEquals(AuthError.NotAuthenticated, error)
    }

    @Test
    fun `a NIF or license number already registered is rejected and nothing is linked`() = runTest {
        registerAina()
        repository.login("ana@clinica.es", PASSWORD, false).getOrThrow()
        repository.completeProfessionalProfile("12345678Z", MADRID, "28/1234", "Aina", "García").getOrThrow()
        repository.register("Otra", "Persona", "otra@clinica.es", PASSWORD).getOrThrow()
        repository.login("otra@clinica.es", PASSWORD, false).getOrThrow()

        val sameNif = repository.completeProfessionalProfile("12345678z", MADRID, "99/9999", "Otra", "Persona")
        val sameLicense = repository.completeProfessionalProfile("X1234567L", MADRID, "28/1234", "Otra", "Persona")

        assertEquals(AuthError.NifAlreadyRegistered, sameNif.exceptionOrNull())
        assertEquals(AuthError.LicenseNumberAlreadyRegistered, sameLicense.exceptionOrNull())
        assertNull(repository.observeCurrentAccount().first()?.professionalId)
    }

    @Test
    fun `the same licence number is accepted in another college`() = runTest {
        registerAina()
        repository.login("ana@clinica.es", PASSWORD, false).getOrThrow()
        repository.completeProfessionalProfile("12345678Z", MADRID, "28/1234", "Aina", "García").getOrThrow()
        repository.register("Otra", "Persona", "otra@clinica.es", PASSWORD).getOrThrow()
        repository.login("otra@clinica.es", PASSWORD, false).getOrThrow()

        val account = repository.completeProfessionalProfile("X1234567L", ProfessionalCollege.GALICIA, "28/1234", "Otra", "Persona")

        assertNotNull(account.getOrThrow().professionalId)
    }

    // D5 (2026-10-20): a record created without an account is linked when its owner registers.
    @Test
    fun `a matching record without an account is linked instead of duplicated`() = runTest {
        insertProfessionalWithoutAccount(id = "pro-existing", nif = "12345678Z", license = "28/1234")
        registerAina()
        repository.login("ana@clinica.es", PASSWORD, false).getOrThrow()

        val account = repository.completeProfessionalProfile("12345678z", MADRID, " 28/1234 ", "Aina", "García").getOrThrow()

        assertEquals("pro-existing", account.professionalId)
        assertEquals(1L, database.professionalQueries.searchByNif("%", 1L, 10L).executeAsList().size.toLong())
    }

    @Test
    fun `a record with the same NIF but other licence data or already linked is not taken over`() = runTest {
        insertProfessionalWithoutAccount(id = "pro-existing", nif = "12345678Z", license = "28/1234")
        registerAina()
        repository.login("ana@clinica.es", PASSWORD, false).getOrThrow()

        val otherCollege = repository.completeProfessionalProfile("12345678Z", ProfessionalCollege.GALICIA, "28/1234", "Aina", "García")
        val otherNumber = repository.completeProfessionalProfile("12345678Z", MADRID, "28/9999", "Aina", "García")
        assertEquals(AuthError.NifAlreadyRegistered, otherCollege.exceptionOrNull())
        assertEquals(AuthError.NifAlreadyRegistered, otherNumber.exceptionOrNull())

        repository.completeProfessionalProfile("12345678Z", MADRID, "28/1234", "Aina", "García").getOrThrow()
        repository.register("Otra", "Persona", "otra@clinica.es", PASSWORD).getOrThrow()
        repository.login("otra@clinica.es", PASSWORD, false).getOrThrow()
        val alreadyLinked = repository.completeProfessionalProfile("12345678Z", MADRID, "28/1234", "Otra", "Persona")
        assertEquals(AuthError.NifAlreadyRegistered, alreadyLinked.exceptionOrNull())
    }

    private fun insertProfessionalWithoutAccount(id: String, nif: String, license: String) =
        database.professionalQueries.insertProfessional(
            id, nif, MADRID.name, license, license, "Aina", "García", "aina garcia", null, null, null, null, now, now,
        )

    private class InMemorySessionStore : SessionStore {
        var accountId: String? = null
        var lastActivityAt: Long? = null

        override fun accountId() = accountId
        override fun lastActivityAt() = lastActivityAt
        override fun save(accountId: String, lastActivityAt: Long) {
            this.accountId = accountId
            this.lastActivityAt = lastActivityAt
        }

        override fun saveLastActivity(lastActivityAt: Long) {
            this.lastActivityAt = lastActivityAt
        }

        override fun clear() {
            accountId = null
            lastActivityAt = null
        }
    }

    private companion object {
        const val PASSWORD = "Fisio-Clinica-2026"
        const val NEW_PASSWORD = "Nueva-Clave-2026!"
        val FAST_HASHER = Pbkdf2PasswordHasher(iterations = 1_000)
    }
}
