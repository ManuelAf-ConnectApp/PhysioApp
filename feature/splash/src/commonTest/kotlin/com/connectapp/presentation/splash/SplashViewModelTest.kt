package com.connectapp.presentation.splash

import com.connectapp.domain.model.Account
import com.connectapp.domain.policy.EpochClock
import com.connectapp.domain.usecase.ExpireIdleSessionUseCase
import com.connectapp.domain.usecase.RestoreSessionUseCase
import com.connectapp.presentation.splash.fake.FakeAuthRepository
import com.connectapp.presentation.splash.fake.accountWithProfile
import com.connectapp.presentation.splash.fake.accountWithoutProfile
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

@OptIn(ExperimentalCoroutinesApi::class)
class SplashViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val now = 1_000_000L

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun TestScope.effectsFor(restored: Account?, lastActivity: Long? = now): List<SplashEffect> {
        val repository = FakeAuthRepository(restoredAccount = restored, lastActivity = lastActivity)
        val clock = EpochClock { now }
        val viewModel = SplashViewModel(RestoreSessionUseCase(repository, ExpireIdleSessionUseCase(repository, clock)))
        val effects = mutableListOf<SplashEffect>()
        val job = launch { viewModel.effect.toList(effects) }
        advanceUntilIdle()
        job.cancel()
        return effects
    }

    @Test
    fun `no remembered session goes to login`() = runTest {
        assertEquals(listOf<SplashEffect>(SplashEffect.NavigateToLogin), effectsFor(restored = null))
    }

    @Test
    fun `a remembered session goes to home or to the onboarding`() = runTest {
        assertEquals(listOf<SplashEffect>(SplashEffect.NavigateToHome), effectsFor(accountWithProfile))
        assertEquals(listOf<SplashEffect>(SplashEffect.NavigateToOnboarding), effectsFor(accountWithoutProfile))
    }

    @Test
    fun `an expired remembered session goes to login`() = runTest {
        assertEquals(listOf<SplashEffect>(SplashEffect.NavigateToLogin), effectsFor(accountWithProfile, lastActivity = 0))
    }
}
