package com.connectapp.presentation.register

import com.connectapp.commonresources.register_error_email_already_registered
import com.connectapp.domain.model.AuthError
import com.connectapp.domain.usecase.RegisterUseCase
import com.connectapp.domain.validation.AuthField
import com.connectapp.presentation.register.fake.FakeAuthRepository
import com.connectapp.presentation.register.fake.RECOVERY_CODE
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
class RegisterViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val repository = FakeAuthRepository()
    private val viewModel = RegisterViewModel(RegisterUseCase(repository))

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun fillValidForm() {
        viewModel.onIntent(RegisterIntent.FirstNameChanged("Aina"))
        viewModel.onIntent(RegisterIntent.LastNameChanged("García"))
        viewModel.onIntent(RegisterIntent.EmailChanged("aina@clinica.es"))
        viewModel.onIntent(RegisterIntent.PasswordChanged(PASSWORD))
        viewModel.onIntent(RegisterIntent.ConfirmPasswordChanged(PASSWORD))
    }

    // "el botón Registrar se habilita" only with valid data.
    @Test
    fun `register is disabled until every field is valid`() {
        assertFalse(viewModel.state.value.canSubmit)

        fillValidForm()
        assertTrue(viewModel.state.value.canSubmit)

        viewModel.onIntent(RegisterIntent.FirstNameChanged("   "))
        assertFalse(viewModel.state.value.canSubmit)
        assertEquals(setOf(AuthField.FIRST_NAME), viewModel.state.value.invalidFields)
    }

    @Test
    fun `a click with an invalid form does not reach the repository`() = runTest {
        viewModel.onIntent(RegisterIntent.RegisterClicked)
        advanceUntilIdle()

        assertEquals(0, repository.registerCalls)
    }

    // D4: the account is created and the recovery code is shown once; no session, no navigation.
    @Test
    fun `a successful registration shows the recovery code and drops the passwords`() = runTest {
        val effects = mutableListOf<RegisterEffect>()
        val job = launch { viewModel.effect.toList(effects) }
        fillValidForm()

        viewModel.onIntent(RegisterIntent.RegisterClicked)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(RECOVERY_CODE, state.recoveryCode)
        assertEquals("", state.password)
        assertEquals("", state.confirmPassword)
        assertTrue(effects.isEmpty())
        assertNull(repository.account.value)
        job.cancel()
    }

    @Test
    fun `continuing requires confirming that the code is stored`() = runTest {
        val effects = mutableListOf<RegisterEffect>()
        val job = launch { viewModel.effect.toList(effects) }
        fillValidForm()
        viewModel.onIntent(RegisterIntent.RegisterClicked)
        advanceUntilIdle()

        viewModel.onIntent(RegisterIntent.ContinueClicked)
        advanceUntilIdle()
        assertTrue(effects.isEmpty())

        viewModel.onIntent(RegisterIntent.RecoveryCodeConfirmed(true))
        viewModel.onIntent(RegisterIntent.ContinueClicked)
        advanceUntilIdle()
        assertEquals(listOf<RegisterEffect>(RegisterEffect.NavigateToLogin), effects)
        assertNull(viewModel.state.value.recoveryCode)
        job.cancel()
    }

    @Test
    fun `a duplicate email shows its message`() = runTest {
        repository.registerResult = Result.failure(AuthError.EmailAlreadyRegistered)
        fillValidForm()

        viewModel.onIntent(RegisterIntent.RegisterClicked)
        advanceUntilIdle()

        assertEquals(register_error_email_already_registered, viewModel.state.value.errorMessage)
        assertNull(viewModel.state.value.recoveryCode)
        assertFalse(viewModel.state.value.isLoading)
    }

    private companion object {
        const val PASSWORD = "Fisio-Clinica-2026"
    }
}
