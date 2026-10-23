package com.connectapp.presentation.profile

import com.connectapp.domain.model.ProfessionalProfile
import com.connectapp.domain.shared.professional.ProfessionalCollege
import com.connectapp.domain.usecase.LogoutUseCase
import com.connectapp.domain.usecase.ObserveCurrentAccountUseCase
import com.connectapp.domain.usecase.ObserveCurrentProfessionalUseCase
import com.connectapp.presentation.profile.fake.FakeAuthRepository
import com.connectapp.presentation.profile.fake.accountWithProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val professional = ProfessionalProfile("pro-1", "12345678Z", ProfessionalCollege.MADRID, "28/1234", "Aina", "García")
    private val repository = FakeAuthRepository().apply {
        account.value = accountWithProfile
        this.professional.value = this@ProfileViewModelTest.professional
    }
    private val viewModel by lazy {
        ProfileViewModel(
            ObserveCurrentAccountUseCase(repository),
            ObserveCurrentProfessionalUseCase(repository),
            LogoutUseCase(repository),
        )
    }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `shows the signed-in account and its professional profile`() = runTest {
        viewModel
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals(accountWithProfile, state.account)
        assertEquals(professional, state.professional)
    }

    @Test
    fun `logout ends the session and goes to login`() = runTest {
        val effects = mutableListOf<ProfileEffect>()
        val job = launch { viewModel.effect.toList(effects) }

        viewModel.onIntent(ProfileIntent.LogoutClicked)
        advanceUntilIdle()

        assertEquals(listOf<ProfileEffect>(ProfileEffect.NavigateToLogin), effects)
        assertEquals(1, repository.logoutCalls)
        assertNull(viewModel.state.value.account)
        job.cancel()
    }
}
