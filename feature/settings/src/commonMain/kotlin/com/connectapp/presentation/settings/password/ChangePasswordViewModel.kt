package com.connectapp.presentation.settings.password

import androidx.lifecycle.viewModelScope
import com.connectapp.commonresources.error_unexpected
import com.connectapp.commonresources.error_wrong_password
import com.connectapp.core.mvi.MviViewModel
import com.connectapp.domain.model.AuthError
import com.connectapp.domain.usecase.ChangePasswordUseCase
import kotlinx.coroutines.launch

/**
 * Settings → change password (D15): current password plus the new one twice (D16). Once changed,
 * the typed passwords are dropped from the state and a confirmation is shown.
 */
class ChangePasswordViewModel(
    private val changePasswordUseCase: ChangePasswordUseCase,
) : MviViewModel<ChangePasswordState, ChangePasswordIntent, ChangePasswordEffect>(ChangePasswordState()) {

    override fun reduce(intent: ChangePasswordIntent) {
        when (intent) {
            is ChangePasswordIntent.CurrentPasswordChanged ->
                updateState { it.copy(currentPassword = intent.value, errorMessage = null) }
            is ChangePasswordIntent.NewPasswordChanged ->
                updateState { it.copy(newPassword = intent.value, errorMessage = null) }
            is ChangePasswordIntent.ConfirmPasswordChanged ->
                updateState { it.copy(confirmPassword = intent.value, errorMessage = null) }
            ChangePasswordIntent.SaveClicked -> save()
        }
    }

    private fun save() {
        val form = state.value
        if (!form.canSubmit) return
        updateState { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            changePasswordUseCase(form.currentPassword, form.newPassword, form.confirmPassword)
                .onSuccess { updateState { ChangePasswordState(isDone = true) } }
                .onFailure { error ->
                    when (error) {
                        is AuthError.AccountLocked, AuthError.NotAuthenticated -> {
                            updateState { ChangePasswordState() }
                            emitEffect(ChangePasswordEffect.NavigateToLogin)
                        }
                        AuthError.InvalidCredentials -> updateState {
                            it.copy(isSaving = false, currentPassword = "", errorMessage = error_wrong_password)
                        }
                        else -> updateState { it.copy(isSaving = false, errorMessage = error_unexpected) }
                    }
                }
        }
    }
}
