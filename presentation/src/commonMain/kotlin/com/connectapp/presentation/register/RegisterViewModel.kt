/*
Author: Manuel María Alconchel Fernández
E-mail: incidencias@connectapp.es
Date: 30/06/2006

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.connectapp.presentation.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.connectapp.domain.usecase.RegisterUseCase
import com.connectapp.domain.validator.FormValidator
import com.connectapp.domain.validator.model.ValidationError
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val registerUseCase: RegisterUseCase,
    private val formValidator: FormValidator
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterState.EMPTY)
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
        _state.update {
            it.copy(
                firstName = intent.firstName,
                firstNameError = formValidator.validateName(intent.firstName)
            )
        }
    }

    private fun onLastNameChanged(intent: RegisterIntent.LastNameChanged) {
        _state.update {
            it.copy(
                lastName = intent.lastName,
                lastNameError = formValidator.validateSurname(intent.lastName)
            )
        }
    }

    private fun onEmailChanged(intent: RegisterIntent.EmailChanged) {
        _state.update {
            it.copy(
                email = intent.email,
                emailError = formValidator.validateEmail(intent.email)
            )
        }
    }

    private fun onPasswordChanged(intent: RegisterIntent.PasswordChanged) {
        _state.update {
            val passwordError = formValidator.validatePassword(intent.password)
            val confirmPasswordError = if (it.confirmPassword.isNotEmpty() && intent.password != it.confirmPassword) {
                ValidationError.PASSWORDS_NOT_MATCH
            } else if (it.confirmPassword.isNotEmpty()) {
                null
            } else {
                it.confirmPasswordError
            }
            it.copy(
                password = intent.password,
                passwordError = passwordError,
                confirmPasswordError = confirmPasswordError
            )
        }
    }

    private fun onConfirmPasswordChanged(intent: RegisterIntent.ConfirmPasswordChanged) {
        _state.update {
            val confirmPasswordError = when {
                intent.confirmPassword.isEmpty() -> ValidationError.FIELD_EMPTY
                intent.confirmPassword != it.password -> ValidationError.PASSWORDS_NOT_MATCH
                else -> null
            }
            it.copy(
                confirmPassword = intent.confirmPassword,
                confirmPasswordError = confirmPasswordError
            )
        }
    }

    private fun onTogglePasswordVisibility() {
        _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    private fun onToggleConfirmPasswordVisibility() {
        _state.update { it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible) }
    }

    private fun register() {
        if (!state.value.isFormValid) return

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
