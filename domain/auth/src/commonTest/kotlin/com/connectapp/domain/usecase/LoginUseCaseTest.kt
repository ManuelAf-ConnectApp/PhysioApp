package com.connectapp.domain.usecase

import com.connectapp.domain.fake.FakeAuthRepository
import com.connectapp.domain.fake.accountWithProfile
import com.connectapp.domain.model.AuthError
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class LoginUseCaseTest {

    private val repository = FakeAuthRepository()
    private val login = LoginUseCase(repository)

    @Test
    fun `credentials and the remember-session choice reach the repository`() = runTest {
        assertEquals(Result.success(accountWithProfile), login("aina@clinica.es", "secret", rememberSession = true))
        assertEquals(true, repository.lastRememberSession)
    }

    @Test
    fun `blank input fails with the generic invalid credentials error`() = runTest {
        assertEquals(Result.failure(AuthError.InvalidCredentials), login("  ", "secret"))
        assertEquals(Result.failure(AuthError.InvalidCredentials), login("aina@clinica.es", ""))
        assertEquals(0, repository.loginCalls)
    }

    @Test
    fun `repository failures are propagated`() = runTest {
        repository.loginResult = Result.failure(AuthError.AccountLocked(retryAtEpochMillis = 42))

        assertEquals(Result.failure(AuthError.AccountLocked(42)), login("aina@clinica.es", "secret"))
    }
}
