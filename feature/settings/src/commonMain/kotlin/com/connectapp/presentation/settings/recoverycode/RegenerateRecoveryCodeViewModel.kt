package com.connectapp.presentation.settings.recoverycode

import androidx.lifecycle.viewModelScope
import com.connectapp.commonresources.error_unexpected
import com.connectapp.commonresources.error_wrong_password
import com.connectapp.core.mvi.MviViewModel
import com.connectapp.domain.model.AuthError
import com.connectapp.domain.usecase.RegenerateRecoveryCodeUseCase
import kotlinx.coroutines.launch

/**
 * Settings → regenerate recovery code (D15): asks for the password, then shows the new code once;
 * the "Back to Settings" button stays disabled until the user confirms it has been stored.
 */
class RegenerateRecoveryCodeViewModel(
    private val regenerateRecoveryCodeUseCase: RegenerateRecoveryCodeUseCase,
) : MviViewModel<RegenerateRecoveryCodeState, RegenerateRecoveryCodeIntent, RegenerateRecoveryCodeEffect>(
    RegenerateRecoveryCodeState(),
) {

    override fun reduce(intent: RegenerateRecoveryCodeIntent) {
        when (intent) {
            is RegenerateRecoveryCodeIntent.PasswordChanged ->
                updateState { it.copy(password = intent.value, errorMessage = null) }
            RegenerateRecoveryCodeIntent.GenerateClicked -> generate()
            is RegenerateRecoveryCodeIntent.NewRecoveryCodeConfirmed ->
                updateState { it.copy(newRecoveryCodeConfirmed = intent.confirmed) }
            RegenerateRecoveryCodeIntent.DoneClicked -> done()
        }
    }

    private fun generate() {
        val form = state.value
        if (!form.canSubmit) return
        updateState { it.copy(isGenerating = true, errorMessage = null) }
        viewModelScope.launch {
            regenerateRecoveryCodeUseCase(form.password)
                .onSuccess { code -> updateState { RegenerateRecoveryCodeState(newRecoveryCode = code) } }
                .onFailure { error ->
                    when (error) {
                        is AuthError.AccountLocked, AuthError.NotAuthenticated -> {
                            updateState { RegenerateRecoveryCodeState() }
                            emitEffect(RegenerateRecoveryCodeEffect.NavigateToLogin)
                        }
                        AuthError.InvalidCredentials -> updateState {
                            it.copy(isGenerating = false, password = "", errorMessage = error_wrong_password)
                        }
                        else -> updateState { it.copy(isGenerating = false, errorMessage = error_unexpected) }
                    }
                }
        }
    }

    private fun done() {
        val current = state.value
        if (current.newRecoveryCode == null || !current.newRecoveryCodeConfirmed) return
        updateState { RegenerateRecoveryCodeState() }
        emitEffect(RegenerateRecoveryCodeEffect.Done)
    }
}
