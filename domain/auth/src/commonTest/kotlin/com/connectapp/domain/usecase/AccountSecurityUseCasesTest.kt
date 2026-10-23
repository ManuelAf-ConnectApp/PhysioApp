package com.connectapp.domain.usecase

import com.connectapp.domain.fake.FakeAuthRepository
import com.connectapp.domain.fake.RECOVERY_CODE
import com.connectapp.domain.model.AuthError
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AccountSecurityUseCasesTest {

    private val repository = FakeAuthRepository()
    private val changePassword = ChangePasswordUseCase(repository)
    private val regenerateCode = RegenerateRecoveryCodeUseCase(repository)

    @Test
    fun `a valid change reaches the repository with the current and new passwords`() = runTest {
        assertEquals(Result.success(Unit), changePassword(CURRENT, NEW, NEW))
        assertEquals(listOf(CURRENT, NEW), repository.lastChangePasswordArgs)
    }

    @Test
    fun `a change with no current password or a new one breaking the policy is rejected`() = runTest {
        assertEquals(Result.failure(AuthError.InvalidInput), changePassword("", NEW, NEW))
        assertEquals(Result.failure(AuthError.InvalidInput), changePassword(CURRENT, "corta", "corta"))
        assertEquals(Result.failure(AuthError.InvalidInput), changePassword(CURRENT, NEW, "Otra-Clave-2026"))
        assertNull(repository.lastChangePasswordArgs)
    }

    @Test
    fun `a wrong current password is propagated`() = runTest {
        repository.changePasswordResult = Result.failure(AuthError.InvalidCredentials)

        assertEquals(Result.failure(AuthError.InvalidCredentials), changePassword(CURRENT, NEW, NEW))
    }

    @Test
    fun `regenerating the code returns the new one`() = runTest {
        assertEquals(Result.success(RECOVERY_CODE), regenerateCode(CURRENT))
        assertEquals(CURRENT, repository.lastRegeneratePassword)
    }

    @Test
    fun `regenerating without a password does not reach the repository`() = runTest {
        assertEquals(Result.failure(AuthError.InvalidInput), regenerateCode(""))
        assertNull(repository.lastRegeneratePassword)
    }

    private companion object {
        const val CURRENT = "Actual-Clave-2026"
        const val NEW = "Nueva-Clave-2026"
    }
}
