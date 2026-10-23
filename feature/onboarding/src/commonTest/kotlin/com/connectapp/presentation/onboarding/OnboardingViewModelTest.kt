package com.connectapp.presentation.onboarding

import com.connectapp.commonresources.error_nif_already_registered
import com.connectapp.domain.model.AuthError
import com.connectapp.domain.shared.professional.ProfessionalCollege
import com.connectapp.domain.usecase.CompleteProfessionalProfileUseCase
import com.connectapp.domain.usecase.LogoutUseCase
import com.connectapp.domain.usecase.ObserveCurrentAccountUseCase
import com.connectapp.presentation.onboarding.fake.FakeAuthRepository
import com.connectapp.presentation.onboarding.fake.accountWithoutProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val repository = FakeAuthRepository().apply { account.value = accountWithoutProfile }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = OnboardingViewModel(
        CompleteProfessionalProfileUseCase(repository),
        LogoutUseCase(repository),
        ObserveCurrentAccountUseCase(repository),
    )

    private fun TestScope.collect(viewModel: OnboardingViewModel, block: () -> Unit): List<OnboardingEffect> {
        val effects = mutableListOf<OnboardingEffect>()
        val job = launch { viewModel.effect.toList(effects) }
        advanceUntilIdle()
        block()
        advanceUntilIdle()
        job.cancel()
        return effects
    }

    @Test
    fun `names are prefilled from the account`() = runTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        assertEquals("Aina", viewModel.state.value.firstName)
        assertEquals("García", viewModel.state.value.lastName)
        assertFalse(viewModel.state.value.canSubmit)
    }

    @Test
    fun `a valid profile is saved and goes to home`() = runTest {
        val viewModel = viewModel()

        val effects = collect(viewModel) {
            viewModel.onIntent(OnboardingIntent.NifChanged("12345678Z"))
            viewModel.onIntent(OnboardingIntent.LicenseNumberChanged("28/1234"))
            assertFalse(viewModel.state.value.canSubmit) // D9: the college is required too
            viewModel.onIntent(OnboardingIntent.CollegeSelected(ProfessionalCollege.MADRID))
            assertTrue(viewModel.state.value.canSubmit)
            viewModel.onIntent(OnboardingIntent.SaveClicked)
        }

        assertEquals(listOf<OnboardingEffect>(OnboardingEffect.NavigateToHome), effects)
        assertEquals(listOf("12345678Z", "MADRID", "28/1234", "Aina", "García"), repository.lastProfileArgs)
    }

    @Test
    fun `an invalid NIF keeps save disabled`() = runTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.onIntent(OnboardingIntent.NifChanged("12345678A"))
        viewModel.onIntent(OnboardingIntent.CollegeSelected(ProfessionalCollege.MADRID))
        viewModel.onIntent(OnboardingIntent.LicenseNumberChanged("28/1234"))
        viewModel.onIntent(OnboardingIntent.SaveClicked)
        advanceUntilIdle()

        assertFalse(viewModel.state.value.canSubmit)
        assertEquals(0, repository.completeProfileCalls)
    }

    @Test
    fun `a duplicate NIF shows its message`() = runTest {
        repository.completeProfileResult = Result.failure(AuthError.NifAlreadyRegistered)
        val viewModel = viewModel()

        val effects = collect(viewModel) {
            viewModel.onIntent(OnboardingIntent.NifChanged("12345678Z"))
            viewModel.onIntent(OnboardingIntent.CollegeSelected(ProfessionalCollege.MADRID))
            viewModel.onIntent(OnboardingIntent.LicenseNumberChanged("28/1234"))
            viewModel.onIntent(OnboardingIntent.SaveClicked)
        }

        assertTrue(effects.isEmpty())
        assertEquals(error_nif_already_registered, viewModel.state.value.errorMessage)
    }

    @Test
    fun `without a session it goes back to login`() = runTest {
        repository.account.value = null

        assertEquals(listOf<OnboardingEffect>(OnboardingEffect.NavigateToLogin), collect(viewModel()) {})
    }

    @Test
    fun `logout ends the session`() = runTest {
        val viewModel = viewModel()

        val effects = collect(viewModel) { viewModel.onIntent(OnboardingIntent.LogoutClicked) }

        assertEquals(listOf<OnboardingEffect>(OnboardingEffect.NavigateToLogin), effects)
        assertEquals(1, repository.logoutCalls)
    }
}
