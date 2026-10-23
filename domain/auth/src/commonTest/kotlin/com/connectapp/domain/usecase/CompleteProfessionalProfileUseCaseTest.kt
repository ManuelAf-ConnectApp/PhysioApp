package com.connectapp.domain.usecase

import com.connectapp.domain.fake.FakeAuthRepository
import com.connectapp.domain.fake.accountWithProfile
import com.connectapp.domain.model.AuthError
import com.connectapp.domain.shared.professional.ProfessionalCollege
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class CompleteProfessionalProfileUseCaseTest {

    private val repository = FakeAuthRepository()
    private val complete = CompleteProfessionalProfileUseCase(repository)

    @Test
    fun `valid data creates the profile`() = runTest {
        assertEquals(Result.success(accountWithProfile), complete("12345678Z", ProfessionalCollege.MADRID, "28/1234", " Aina ", "García"))
        assertEquals(listOf("12345678Z", "MADRID", "28/1234", "Aina", "García"), repository.lastProfileArgs)
    }

    @Test
    fun `an invalid NIF or license number is rejected without calling the repository`() = runTest {
        assertEquals(Result.failure(AuthError.InvalidInput), complete("12345678A", ProfessionalCollege.MADRID, "28/1234", "Aina", "García"))
        assertEquals(Result.failure(AuthError.InvalidInput), complete("12345678Z", ProfessionalCollege.MADRID, "", "Aina", "García"))
        assertEquals(Result.failure(AuthError.InvalidInput), complete("12345678Z", ProfessionalCollege.MADRID, "28/1234", " ", "García"))
        assertEquals(Result.failure(AuthError.InvalidInput), complete("12345678Z", null, "28/1234", "Aina", "García"))
        assertEquals(0, repository.completeProfileCalls)
    }
}
