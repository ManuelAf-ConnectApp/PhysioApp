package com.connectapp.presentation.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.connectapp.domain.usecase.RegisterUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterState())
    val state: StateFlow<RegisterState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<RegisterEffect>()
    val effect: SharedFlow<RegisterEffect> = _effect.asSharedFlow()

    fun onIntent(intent: RegisterIntent) {
        when (intent) {
            is RegisterIntent.FirstNameChanged -> onFirstNameChanged(intent)
            is RegisterIntent.LastNameChanged -> onLastNameChanged(intent)
            is RegisterIntent.EmailChanged -> onEmailChanged(intent)
            is RegisterIntent.PasswordChanged -> onPasswordChanged(intent)
            is RegisterIntent.ConfirmPasswordChanged -> onConfirmPasswordChanged(intent)
            RegisterIntent.RegisterClicked -> register()
            RegisterIntent.LoginClicked -> emitEffect(RegisterEffect.NavigateToLogin)
            RegisterIntent.TogglePasswordVisibility -> onTogglePasswordVisibility()
            RegisterIntent.ToggleConfirmPasswordVisibility -> onToggleConfirmPasswordVisibility()
        }
    }

    private fun onFirstNameChanged(intent: RegisterIntent.FirstNameChanged) {
        _state.update { it.copy(firstName = intent.firstName) }
    }

    private fun onLastNameChanged(intent: RegisterIntent.LastNameChanged) {
        _state.update { it.copy(lastName = intent.lastName) }
    }

    private fun onEmailChanged(intent: RegisterIntent.EmailChanged) {
        _state.update { it.copy(email = intent.email) }
    }

    private fun onPasswordChanged(intent: RegisterIntent.PasswordChanged) {
        _state.update { it.copy(password = intent.password) }
    }

    private fun onConfirmPasswordChanged(intent: RegisterIntent.ConfirmPasswordChanged) {
        _state.update { it.copy(confirmPassword = intent.confirmPassword) }
    }

    private fun onTogglePasswordVisibility() {
        _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    private fun onToggleConfirmPasswordVisibility() {
        _state.update { it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible) }
    }

    private fun register() {
        if (_state.value.password != _state.value.confirmPassword) {
            _state.update { it.copy(errorMessage = "Passwords do not match") }
            return
        }

        _state.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            registerUseCase(
                _state.value.firstName,
                _state.value.lastName,
                _state.value.email,
                _state.value.password
            )
                .onSuccess {
                    _state.update { it.copy(isLoading = false, isRegistered = true) }
                    emitEffect(RegisterEffect.NavigateToLogin)
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false, errorMessage = error.message) }
                }
        }
    }

    private fun emitEffect(effect: RegisterEffect) {
        viewModelScope.launch {
            _effect.emit(effect)
        }
    }
}
