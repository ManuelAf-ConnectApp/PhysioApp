package com.connectapp.domain.repository

import com.connectapp.domain.model.Account
import com.connectapp.domain.model.ProfessionalProfile
import com.connectapp.domain.shared.professional.ProfessionalCollege
import kotlinx.coroutines.flow.Flow

// Accounts, credentials and the session are one consistent unit over the same table and the same
// lockout counter; splitting them only to meet the function count would scatter that logic.
/**
 * Local accounts and the in-app session (docs/00-auditoria-y-plan.md §14, phase 4). Everything is
 * verified against the encrypted on-device database: there is no backend.
 *
 * Inputs are expected to be validated already (see [com.connectapp.domain.validation.AuthValidation]);
 * implementations normalize emails, NIFs and recovery codes themselves. Failures are always
 * [com.connectapp.domain.model.AuthError]s.
 */
@Suppress("TooManyFunctions")
interface AuthRepository {
    /**
     * Creates an account with the password and a new one-time recovery code, both stored only as
     * hashes, and returns the recovery code in plain text (D4). Does not sign in.
     */
    suspend fun register(firstName: String, lastName: String, email: String, password: String): Result<String>

    /**
     * Verifies the credentials, applying the failed-attempt lockout, and starts a session.
     * [rememberSession] keeps the session across app restarts (still subject to the idle timeout).
     */
    suspend fun login(email: String, password: String, rememberSession: Boolean): Result<Account>

    /**
     * Replaces the password when [recoveryCode] matches the account of [email], invalidates that
     * code and returns a new one (D4). Does not sign in.
     */
    suspend fun resetPassword(email: String, recoveryCode: String, newPassword: String): Result<String>

    /**
     * Replaces the password of the signed-in account once [currentPassword] is verified (D15).
     * A wrong current password counts as a failed login; when that reaches the lockout, the
     * session also ends ([com.connectapp.domain.model.AuthError.AccountLocked]).
     */
    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit>

    /**
     * Issues a new one-time recovery code for the signed-in account once [password] is verified
     * and returns it in plain text; the previous code stops working (D15). Wrong passwords count
     * as in [changePassword].
     */
    suspend fun regenerateRecoveryCode(password: String): Result<String>

    /**
     * Checks the password of the signed-in account before a sensitive action, such as restoring a
     * backup over this device's data. Wrong passwords count as in [changePassword].
     */
    suspend fun verifyPassword(password: String): Result<Unit>

    /** Whether this device has any account; the sign-in screen offers restoring a backup when not. */
    suspend fun hasAccounts(): Boolean

    /**
     * Creates the professional record of the signed-in account and links it (D3). When a record
     * created earlier without an account has the same NIF, college and licence number and no
     * account yet, that record is linked instead (D5, confirmed 2026-10-20).
     */
    suspend fun completeProfessionalProfile(
        nif: String,
        college: ProfessionalCollege,
        licenseNumber: String,
        firstName: String,
        lastName: String,
    ): Result<Account>

    /** Restores a remembered session after an app restart, or returns null. */
    suspend fun restoreSession(): Account?

    /** The signed-in account, or null when signed out. */
    fun observeCurrentAccount(): Flow<Account?>

    fun observeCurrentProfessional(): Flow<ProfessionalProfile?>

    /** Marks user interaction now, which postpones the idle timeout. No-op when signed out. */
    fun recordActivity()

    /** Epoch millis of the last recorded interaction of the current session, or null when signed out. */
    fun lastActivityAt(): Long?

    /** Ends the session and forgets any remembered session. Never fails. */
    fun logout()
}
