package com.connectapp.presentation.profile.fake

import com.connectapp.domain.model.Account
import com.connectapp.domain.model.ProfessionalProfile
import com.connectapp.domain.repository.AuthRepository
import com.connectapp.domain.shared.professional.ProfessionalCollege
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal val accountWithoutProfile = Account("acc-1", "Aina", "García", "aina@clinica.es", professionalId = null)
internal val accountWithProfile = accountWithoutProfile.copy(professionalId = "pro-1")
internal const val RECOVERY_CODE = "23456789ABCDEFGHJKLM"

/** Configurable in-memory [AuthRepository] that records how it was called. */
internal class FakeAuthRepository(
    var registerResult: Result<String> = Result.success(RECOVERY_CODE),
    var loginResult: Result<Account> = Result.success(accountWithProfile),
    var resetResult: Result<String> = Result.success(RECOVERY_CODE),
    var completeProfileResult: Result<Account> = Result.success(accountWithProfile),
    var restoredAccount: Account? = null,
    var lastActivity: Long? = null,
) : AuthRepository {
    val account = MutableStateFlow<Account?>(null)
    val professional = MutableStateFlow<ProfessionalProfile?>(null)

    var registerCalls = 0
        private set
    var loginCalls = 0
        private set
    var resetCalls = 0
        private set
    var completeProfileCalls = 0
        private set
    var logoutCalls = 0
        private set
    var recordActivityCalls = 0
        private set
    var lastRegisterArgs: List<String>? = null
        private set
    var lastRememberSession: Boolean? = null
        private set
    var lastProfileArgs: List<String>? = null
        private set

    override suspend fun register(firstName: String, lastName: String, email: String, password: String): Result<String> {
        registerCalls++
        lastRegisterArgs = listOf(firstName, lastName, email, password)
        return registerResult
    }

    override suspend fun login(email: String, password: String, rememberSession: Boolean): Result<Account> {
        loginCalls++
        lastRememberSession = rememberSession
        return loginResult.onSuccess { account.value = it }
    }

    override suspend fun resetPassword(email: String, recoveryCode: String, newPassword: String): Result<String> {
        resetCalls++
        return resetResult
    }

    override suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> =
        Result.success(Unit)

    override suspend fun regenerateRecoveryCode(password: String): Result<String> =
        Result.success("23456-789AB-CDEFG-HJKLM")

    var hasAccountsResult = true

    override suspend fun verifyPassword(password: String): Result<Unit> = Result.success(Unit)

    override suspend fun hasAccounts(): Boolean = hasAccountsResult

    override suspend fun completeProfessionalProfile(
        nif: String,
        college: ProfessionalCollege,
        licenseNumber: String,
        firstName: String,
        lastName: String,
    ): Result<Account> {
        completeProfileCalls++
        lastProfileArgs = listOf(nif, college.name, licenseNumber, firstName, lastName)
        return completeProfileResult.onSuccess { account.value = it }
    }

    override suspend fun restoreSession(): Account? = restoredAccount?.also { account.value = it }

    override fun observeCurrentAccount(): Flow<Account?> = account

    override fun observeCurrentProfessional(): Flow<ProfessionalProfile?> = professional

    override fun recordActivity() {
        recordActivityCalls++
    }

    override fun lastActivityAt(): Long? = lastActivity

    override fun logout() {
        logoutCalls++
        account.value = null
        lastActivity = null
    }
}
