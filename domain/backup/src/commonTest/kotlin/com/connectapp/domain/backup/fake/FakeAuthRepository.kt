package com.connectapp.domain.backup.fake

import com.connectapp.domain.model.Account
import com.connectapp.domain.model.ProfessionalProfile
import com.connectapp.domain.repository.AuthRepository
import com.connectapp.domain.shared.professional.ProfessionalCollege
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/** Only the password check and the logout matter to backups. */
internal class FakeAuthRepository(
    var verifyPasswordResult: Result<Unit> = Result.success(Unit),
) : AuthRepository {
    var verifiedPasswords = mutableListOf<String>()
        private set
    var logoutCalls = 0
        private set

    override suspend fun verifyPassword(password: String): Result<Unit> {
        verifiedPasswords += password
        return verifyPasswordResult
    }

    override fun logout() {
        logoutCalls++
    }

    override suspend fun hasAccounts(): Boolean = true

    override suspend fun register(firstName: String, lastName: String, email: String, password: String) =
        error("not used")

    override suspend fun login(email: String, password: String, rememberSession: Boolean): Result<Account> =
        error("not used")

    override suspend fun resetPassword(email: String, recoveryCode: String, newPassword: String): Result<String> =
        error("not used")

    override suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> =
        error("not used")

    override suspend fun regenerateRecoveryCode(password: String): Result<String> = error("not used")

    override suspend fun completeProfessionalProfile(
        nif: String,
        college: ProfessionalCollege,
        licenseNumber: String,
        firstName: String,
        lastName: String,
    ): Result<Account> = error("not used")

    override suspend fun restoreSession(): Account? = null

    override fun observeCurrentAccount(): Flow<Account?> = flowOf(null)

    override fun observeCurrentProfessional(): Flow<ProfessionalProfile?> = flowOf(null)

    override fun recordActivity() = Unit

    override fun lastActivityAt(): Long? = null
}
