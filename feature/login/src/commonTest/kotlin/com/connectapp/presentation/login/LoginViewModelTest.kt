package com.connectapp.presentation.login

import com.connectapp.commonresources.error_unexpected
import com.connectapp.commonresources.login_error_invalid_credentials
import com.connectapp.commonresources.login_error_locked
import com.connectapp.domain.model.AuthError
import com.connectapp.domain.policy.EpochClock
import com.connectapp.domain.usecase.HasAccountsUseCase
import com.connectapp.domain.usecase.LoginUseCase
import com.connectapp.presentation.login.fake.FakeAuthRepository
import com.connectapp.presentation.login.fake.accountWithProfile
import com.connectapp.presentation.login.fake.accountWithoutProfile
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
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val now = 1_000_000L
    private val repository = FakeAuthRepository()
    private val viewModel by lazy { LoginViewModel(LoginUseCase(repository), EpochClock { now }, HasAccountsUseCase(repository)) }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun TestScope.submit(email: String = "aina@clinica.es", password: String = "Fisio-Clinica-2026"): List<LoginEffect> {
        val effects = mutableListOf<LoginEffect>()
        val job = launch { viewModel.effect.toList(effects) }
        viewModel.onIntent(LoginIntent.EmailChanged(email))
        viewModel.onIntent(LoginIntent.PasswordChanged(password))
        viewModel.onIntent(LoginIntent.LoginClicked)
        advanceUntilIdle()
        job.cancel()
        return effects
    }

    @Test
    fun `an account with a professional profile goes to home and the password is dropped`() = runTest {
        repository.loginResult = Result.success(accountWithProfile)

        val effects = submit()

        assertEquals(listOf<LoginEffect>(LoginEffect.NavigateToHome), effects)
        assertEquals("", viewModel.state.value.password)
        assertFalse(viewModel.state.value.isLoading)
    }

    // D3: without a professional profile the account cannot reach Home.
    @Test
    fun `an account without a professional profile goes to the onboarding`() = runTest {
        repository.loginResult = Result.success(accountWithoutProfile)

        assertEquals(listOf<LoginEffect>(LoginEffect.NavigateToOnboarding), submit())
    }

    // A wrong password and an unknown email show the same generic message (the repository
    // reports both as InvalidCredentials).
    @Test
    fun `invalid credentials show the generic message and do not navigate`() = runTest {
        repository.loginResult = Result.failure(AuthError.InvalidCredentials)

        val effects = submit()

        assertTrue(effects.isEmpty())
        assertEquals(login_error_invalid_credentials, viewModel.state.value.errorMessage)
        assertEquals("", viewModel.state.value.password)
    }

    @Test
    fun `a locked account shows the minutes left rounded up`() = runTest {
        repository.loginResult = Result.failure(AuthError.AccountLocked(retryAtEpochMillis = now + 4 * 60_000 + 1))

        submit()

        assertEquals(login_error_locked, viewModel.state.value.errorMessage)
        assertEquals("5", viewModel.state.value.errorArgument)
    }

    @Test
    fun `an unexpected failure shows the generic unexpected error`() = runTest {
        repository.loginResult = Result.failure(AuthError.Unknown(IllegalStateException()))

        submit()

        assertEquals(error_unexpected, viewModel.state.value.errorMessage)
    }

    // Regression guard for the template's removed DEV_ADMIN bypass (phase 2).
    @Test
    fun `the template's former dev admin credentials are not accepted`() = runTest {
        repository.loginResult = Result.failure(AuthError.InvalidCredentials)

        val effects = submit(email = "admin@admin.com", password = "Admin@2026")

        assertTrue(effects.isEmpty())
        assertNull(repository.account.value)
    }

    // D14: "remember session" is off by default and the choice reaches the repository as-is.
    @Test
    fun `remember session is off by default and passed through`() = runTest {
        assertFalse(viewModel.state.value.rememberSessionEnabled)
        submit()
        assertEquals(false, repository.lastRememberSession)

        viewModel.onIntent(LoginIntent.RememberSessionToggled(true))
        submit()
        assertEquals(true, repository.lastRememberSession)
    }

    @Test
    fun `navigation intents emit their effects`() = runTest {
        val effects = mutableListOf<LoginEffect>()
        val job = launch { viewModel.effect.toList(effects) }

        viewModel.onIntent(LoginIntent.RegisterClicked)
        viewModel.onIntent(LoginIntent.ForgotPasswordClicked)
        advanceUntilIdle()

        assertEquals(listOf(LoginEffect.NavigateToRegister, LoginEffect.NavigateToForgotPassword), effects)
        job.cancel()
    }

    @Test
    fun `restoring a backup is only offered on a tablet without accounts`() = runTest {
        repository.hasAccountsResult = false
        viewModel
        advanceUntilIdle()
        assertTrue(viewModel.state.value.canRestoreBackup)

        val effects = mutableListOf<LoginEffect>()
        val job = launch { viewModel.effect.toList(effects) }
        viewModel.onIntent(LoginIntent.RestoreBackupClicked)
        advanceUntilIdle()
        job.cancel()
        assertEquals(listOf<LoginEffect>(LoginEffect.NavigateToRestoreBackup), effects)
    }

    @Test
    fun `a tablet with accounts does not offer restoring a backup`() = runTest {
        viewModel
        advanceUntilIdle()

        assertFalse(viewModel.state.value.canRestoreBackup)
    }
}
