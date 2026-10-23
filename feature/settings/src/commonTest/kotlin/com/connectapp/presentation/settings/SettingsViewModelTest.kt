package com.connectapp.presentation.settings

import com.connectapp.domain.backup.ObserveLastBackupUseCase
import com.connectapp.domain.provider.AppVersionProvider
import com.connectapp.domain.usecase.LogoutUseCase
import com.connectapp.domain.usecase.ObserveCurrentAccountUseCase
import com.connectapp.presentation.settings.fake.FakeAuthRepository
import com.connectapp.presentation.settings.fake.FakeBackupRepository
import com.connectapp.presentation.settings.fake.accountWithProfile
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

private class FakeAppVersionProvider(override val versionName: String = "1.0-test") : AppVersionProvider

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val repository = FakeAuthRepository().apply { account.value = accountWithProfile }
    private val backups = FakeBackupRepository()
    private val viewModel by lazy {
        SettingsViewModel(
            LogoutUseCase(repository),
            ObserveCurrentAccountUseCase(repository),
            FakeAppVersionProvider(),
            ObserveLastBackupUseCase(backups),
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
    fun `shows the account and the app version`() = runTest {
        viewModel
        advanceUntilIdle()

        assertEquals("Aina García", viewModel.state.value.userName)
        assertEquals("aina@clinica.es", viewModel.state.value.userEmail)
        assertEquals("1.0-test", viewModel.state.value.appVersion)
    }

    @Test
    fun `shows the date of the last backup`() = runTest {
        viewModel
        advanceUntilIdle()
        assertEquals(null, viewModel.state.value.lastBackupAt)

        backups.markBackupSaved(1_000L)
        advanceUntilIdle()

        assertEquals(1_000L, viewModel.state.value.lastBackupAt)
    }

    @Test
    fun `logout ends the session and goes to login`() = runTest {
        val effects = mutableListOf<SettingsEffect>()
        val job = launch { viewModel.effect.toList(effects) }

        viewModel.onIntent(SettingsIntent.LogoutClicked)
        advanceUntilIdle()

        assertEquals(listOf<SettingsEffect>(SettingsEffect.NavigateToLogin), effects)
        assertEquals(1, repository.logoutCalls)
        job.cancel()
    }
}
