package com.connectapp.domain.usecase

import com.connectapp.domain.fake.FakeAuthRepository
import com.connectapp.domain.fake.RECOVERY_CODE
import com.connectapp.domain.model.AuthError
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class RegisterUseCaseTest {

    private val repository = FakeAuthRepository()
    private val register = RegisterUseCase(repository)

    private suspend fun registerWith(
        firstName: String = "Aina",
        lastName: String = "García",
        email: String = "aina@clinica.es",
        password: String = PASSWORD,
        confirmation: String = password,
    ) = register(firstName, lastName, email, password, confirmation)

    @Test
    fun `valid data reaches the repository with trimmed names and returns the recovery code`() = runTest {
        val result = registerWith(firstName = "  Aina ", lastName = " García López ")

        assertEquals(Result.success(RECOVERY_CODE), result)
        assertEquals(listOf("Aina", "García López", "aina@clinica.es", PASSWORD), repository.lastRegisterArgs)
    }

    // D9 (2026-10-20): names only need to be non-blank, so "Ana" or "Gil" are accepted.
    @Test
    fun `a blank name is rejected without calling the repository`() = runTest {
        assertEquals(Result.failure(AuthError.InvalidInput), registerWith(firstName = "    "))
        assertEquals(Result.failure(AuthError.InvalidInput), registerWith(lastName = ""))
        assertEquals(0, repository.registerCalls)
    }

    @Test
    fun `short real names are accepted`() = runTest {
        registerWith(firstName = "Ana", lastName = "Gil")
        assertEquals(1, repository.registerCalls)
    }

    @Test
    fun `an invalid email is rejected`() = runTest {
        assertEquals(Result.failure(AuthError.InvalidInput), registerWith(email = "aina@clinica"))
        assertEquals(0, repository.registerCalls)
    }

    @Test
    fun `a password breaking the policy is rejected`() = runTest {
        assertEquals(Result.failure(AuthError.InvalidInput), registerWith(password = "Corta1!"))
        assertEquals(Result.failure(AuthError.InvalidInput), registerWith(password = "sinmayusculas123!"))
        assertEquals(0, repository.registerCalls)
    }

    @Test
    fun `a confirmation that does not match is rejected`() = runTest {
        assertEquals(Result.failure(AuthError.InvalidInput), registerWith(confirmation = "$PASSWORD."))
        assertEquals(0, repository.registerCalls)
    }

    @Test
    fun `repository failures are propagated`() = runTest {
        repository.registerResult = Result.failure(AuthError.EmailAlreadyRegistered)

        assertEquals(Result.failure(AuthError.EmailAlreadyRegistered), registerWith())
    }

    private companion object {
        const val PASSWORD = "Fisio-Clinica-2026"
    }
}
