package com.connectapp.presentation.forgot_password

import androidx.lifecycle.viewModelScope
import com.connectapp.commonresources.error_unexpected
import com.connectapp.commonresources.forgot_password_error_invalid_data
import com.connectapp.core.mvi.MviViewModel
import com.connectapp.domain.model.AuthError
import com.connectapp.domain.usecase.ResetPasswordUseCase
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource

/**
 * Offline password reset with email + one-time recovery code (D4). A wrong code and an unknown
 * email show the same message, so the screen never reveals whether an email is registered. Once
 * the new recovery code is shown, the user must confirm it is stored before leaving.
 */
class ForgotPasswordViewModel(
    private val resetPasswordUseCase: ResetPasswordUseCase,
) : MviViewModel<ForgotPasswordState, ForgotPasswordIntent, ForgotPasswordEffect>(ForgotPasswordState()) {

    override fun reduce(intent: ForgotPasswordIntent) {
        when (intent) {
            is ForgotPasswordIntent.EmailChanged -> updateState { it.copy(email = intent.value, errorMessage = null) }
            is ForgotPasswordIntent.RecoveryCodeChanged ->
                updateState { it.copy(recoveryCode = intent.value, errorMessage = null) }
            is ForgotPasswordIntent.NewPasswordChanged ->
                updateState { it.copy(newPassword = intent.value, errorMessage = null) }
            is ForgotPasswordIntent.ConfirmPasswordChanged ->
                updateState { it.copy(confirmPassword = intent.value, errorMessage = null) }
            ForgotPasswordIntent.ResetClicked -> reset()
            is ForgotPasswordIntent.NewRecoveryCodeConfirmed ->
                updateState { it.copy(newRecoveryCodeConfirmed = intent.confirmed) }
            ForgotPasswordIntent.BackToLoginClicked -> backToLogin()
        }
    }

    private fun reset() {
        val form = state.value
        if (!form.canSubmit) return
        updateState { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            resetPasswordUseCase(form.email, form.recoveryCode, form.newPassword, form.confirmPassword)
                .onSuccess { code -> updateState { ForgotPasswordState(newRecoveryCode = code) } }
                .onFailure { error -> updateState { it.copy(isLoading = false, errorMessage = error.toMessage()) } }
        }
    }

    // While a new code is on screen, leaving requires confirming it has been stored.
    private fun backToLogin() {
        val current = state.value
        if (current.newRecoveryCode != null && !current.newRecoveryCodeConfirmed) return
        updateState { ForgotPasswordState() }
        emitEffect(ForgotPasswordEffect.NavigateToLogin)
    }

    private fun Throwable.toMessage(): StringResource = when (this) {
        AuthError.InvalidRecoveryData -> forgot_password_error_invalid_data
        else -> error_unexpected
    }
}
