package com.connectapp.presentation.forgot_password

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.connectapp.domain.usecase.ForgotPasswordUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ForgotPasswordViewModel(
    private val forgotPasswordUseCase: ForgotPasswordUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ForgotPasswordState())
    val state: StateFlow<ForgotPasswordState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<ForgotPasswordEffect>()
    val effect: SharedFlow<ForgotPasswordEffect> = _effect.asSharedFlow()

    fun onIntent(intent: ForgotPasswordIntent) {
        when (intent) {
            is ForgotPasswordIntent.EmailChanged -> _state.update { it.copy(email = intent.email) }
            ForgotPasswordIntent.SendEmailClicked -> sendEmail()
            ForgotPasswordIntent.BackToLoginClicked -> emitEffect(ForgotPasswordEffect.NavigateToLogin)
        }
    }

    private fun sendEmail() {
        if (_state.value.email.isBlank()) {
            _state.update { it.copy(errorMessage = "Email cannot be empty") }
            return
        }

        _state.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            forgotPasswordUseCase(_state.value.email)
                .onSuccess {
                    _state.update { it.copy(isLoading = false, isEmailSent = true) }
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false, errorMessage = error.message) }
                }
        }
    }

    private fun emitEffect(effect: ForgotPasswordEffect) {
        viewModelScope.launch {
            _effect.emit(effect)
        }
    }
}
