package com.connectapp.presentation.register

import androidx.lifecycle.viewModelScope
import com.connectapp.commonresources.error_unexpected
import com.connectapp.commonresources.register_error_email_already_registered
import com.connectapp.core.mvi.MviViewModel
import com.connectapp.domain.model.AuthError
import com.connectapp.domain.usecase.RegisterUseCase
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource

/**
 * Registration. After success the recovery code is shown once (D4) and the
 * user must confirm it is stored before going on to sign in: registering does not open a session,
 * so an unattended code screen never gives access to the app.
 */
class RegisterViewModel(
    private val registerUseCase: RegisterUseCase,
) : MviViewModel<RegisterState, RegisterIntent, RegisterEffect>(RegisterState()) {

    override fun reduce(intent: RegisterIntent) {
        when (intent) {
            is RegisterIntent.FirstNameChanged -> updateState { it.copy(firstName = intent.value, errorMessage = null) }
            is RegisterIntent.LastNameChanged -> updateState { it.copy(lastName = intent.value, errorMessage = null) }
            is RegisterIntent.EmailChanged -> updateState { it.copy(email = intent.value, errorMessage = null) }
            is RegisterIntent.PasswordChanged -> updateState { it.copy(password = intent.value, errorMessage = null) }
            is RegisterIntent.ConfirmPasswordChanged ->
                updateState { it.copy(confirmPassword = intent.value, errorMessage = null) }
            RegisterIntent.RegisterClicked -> register()
            is RegisterIntent.RecoveryCodeConfirmed -> updateState { it.copy(recoveryCodeConfirmed = intent.confirmed) }
            RegisterIntent.ContinueClicked -> if (state.value.recoveryCodeConfirmed) finish()
            RegisterIntent.LoginClicked -> emitEffect(RegisterEffect.NavigateToLogin)
        }
    }

    private fun register() {
        val form = state.value
        if (!form.canSubmit) return
        updateState { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            registerUseCase(form.firstName, form.lastName, form.email, form.password, form.confirmPassword)
                .onSuccess { code ->
                    // The passwords are no longer needed: drop them from memory.
                    updateState { it.copy(isLoading = false, password = "", confirmPassword = "", recoveryCode = code) }
                }
                .onFailure { error ->
                    updateState { it.copy(isLoading = false, errorMessage = error.toMessage()) }
                }
        }
    }

    private fun finish() {
        updateState { RegisterState() }
        emitEffect(RegisterEffect.NavigateToLogin)
    }

    private fun Throwable.toMessage(): StringResource = when (this) {
        AuthError.EmailAlreadyRegistered -> register_error_email_already_registered
        else -> error_unexpected
    }
}
