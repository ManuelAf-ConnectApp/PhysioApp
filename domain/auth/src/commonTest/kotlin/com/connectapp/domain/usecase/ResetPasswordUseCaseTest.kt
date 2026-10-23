package com.connectapp.domain.usecase

import com.connectapp.domain.fake.FakeAuthRepository
import com.connectapp.domain.fake.RECOVERY_CODE
import com.connectapp.domain.model.AuthError
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ResetPasswordUseCaseTest {

    private val repository = FakeAuthRepository()
    private val reset = ResetPasswordUseCase(repository)

    @Test
    fun `a well-formed request returns the new recovery code`() = runTest {
        assertEquals(Result.success(RECOVERY_CODE), reset("aina@clinica.es", "23456-789ab-cdefg-hjklm", PASSWORD, PASSWORD))
    }

    // Without the recovery code the password cannot be reset, and the error is the same
    // neutral one the repository gives for an unknown email.
    @Test
    fun `a missing or malformed code fails with the neutral error without calling the repository`() = runTest {
        assertEquals(Result.failure(AuthError.InvalidRecoveryData), reset("aina@clinica.es", "", PASSWORD, PASSWORD))
        assertEquals(Result.failure(AuthError.InvalidRecoveryData), reset("aina@clinica.es", "12345", PASSWORD, PASSWORD))
        assertEquals(Result.failure(AuthError.InvalidRecoveryData), reset("no-es-un-email", RECOVERY_CODE, PASSWORD, PASSWORD))
        assertEquals(0, repository.resetCalls)
    }

    @Test
    fun `a new password breaking the policy or its confirmation is rejected`() = runTest {
        assertEquals(Result.failure(AuthError.InvalidInput), reset("aina@clinica.es", RECOVERY_CODE, "corta", "corta"))
        assertEquals(Result.failure(AuthError.InvalidInput), reset("aina@clinica.es", RECOVERY_CODE, PASSWORD, "otra"))
        assertEquals(0, repository.resetCalls)
    }

    @Test
    fun `repository failures are propagated`() = runTest {
        repository.resetResult = Result.failure(AuthError.InvalidRecoveryData)

        assertEquals(Result.failure(AuthError.InvalidRecoveryData), reset("aina@clinica.es", RECOVERY_CODE, PASSWORD, PASSWORD))
    }

    private companion object {
        const val PASSWORD = "Nueva-Clave-2026"
    }
}
