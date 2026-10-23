package com.connectapp.presentation.forgot_password

import com.connectapp.commonresources.forgot_password_error_invalid_data
import com.connectapp.domain.model.AuthError
import com.connectapp.domain.usecase.ResetPasswordUseCase
import com.connectapp.presentation.forgot_password.fake.FakeAuthRepository
import com.connectapp.presentation.forgot_password.fake.RECOVERY_CODE
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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ForgotPasswordViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val repository = FakeAuthRepository()
    private val viewModel = ForgotPasswordViewModel(ResetPasswordUseCase(repository))

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun fillForm(code: String = "23456-789AB-CDEFG-HJKLM") {
        viewModel.onIntent(ForgotPasswordIntent.EmailChanged("aina@clinica.es"))
        viewModel.onIntent(ForgotPasswordIntent.RecoveryCodeChanged(code))
        viewModel.onIntent(ForgotPasswordIntent.NewPasswordChanged(PASSWORD))
        viewModel.onIntent(ForgotPasswordIntent.ConfirmPasswordChanged(PASSWORD))
    }

    // Knowing only the email is not enough to reset the password.
    @Test
    fun `reset is disabled without a well-formed recovery code`() {
        fillForm(code = "")
        assertFalse(viewModel.state.value.canSubmit)

        fillForm()
        assertTrue(viewModel.state.value.canSubmit)
    }

    @Test
    fun `a wrong code or unknown email shows the neutral message`() = runTest {
        repository.resetResult = Result.failure(AuthError.InvalidRecoveryData)
        fillForm()

        viewModel.onIntent(ForgotPasswordIntent.ResetClicked)
        advanceUntilIdle()

        assertEquals(forgot_password_error_invalid_data, viewModel.state.value.errorMessage)
        assertNull(viewModel.state.value.newRecoveryCode)
    }

    @Test
    fun `a successful reset shows the new code and clears the form`() = runTest {
        fillForm()

        viewModel.onIntent(ForgotPasswordIntent.ResetClicked)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(RECOVERY_CODE, state.newRecoveryCode)
        assertEquals("", state.newPassword)
        assertEquals("", state.recoveryCode)
    }

    @Test
    fun `leaving after a reset requires confirming the new code is stored`() = runTest {
        val effects = mutableListOf<ForgotPasswordEffect>()
        val job = launch { viewModel.effect.toList(effects) }
        fillForm()
        viewModel.onIntent(ForgotPasswordIntent.ResetClicked)
        advanceUntilIdle()

        viewModel.onIntent(ForgotPasswordIntent.BackToLoginClicked)
        advanceUntilIdle()
        assertTrue(effects.isEmpty())

        viewModel.onIntent(ForgotPasswordIntent.NewRecoveryCodeConfirmed(true))
        viewModel.onIntent(ForgotPasswordIntent.BackToLoginClicked)
        advanceUntilIdle()
        assertEquals(listOf<ForgotPasswordEffect>(ForgotPasswordEffect.NavigateToLogin), effects)
        job.cancel()
    }

    private companion object {
        const val PASSWORD = "Nueva-Clave-2026"
    }
}
