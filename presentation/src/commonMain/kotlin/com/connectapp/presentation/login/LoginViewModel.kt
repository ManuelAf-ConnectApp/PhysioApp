package com.connectapp.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.connectapp.domain.usecase.GetNotificationPermissionUseCase
import com.connectapp.domain.usecase.LoginUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    private val getNotificationPermissionUseCase: GetNotificationPermissionUseCase
) : ViewModel() {

    private val _state: MutableStateFlow<LoginState> = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> get() = _state.asStateFlow()

    private val _intents: MutableSharedFlow<LoginIntent> = MutableSharedFlow()
    val intents: SharedFlow<LoginIntent> get() = _intents.asSharedFlow()

    private val _effect: MutableSharedFlow<LoginEffect> = MutableSharedFlow()
    val effect: SharedFlow<LoginEffect> get() = _effect.asSharedFlow()

    init {
        viewModelScope.launch {
           getNotificationPermissionUseCase().onSuccess {
               if(!it){

               } else {

               }
           }.onFailure {

           }
        }
    }


    fun onIntent(intent: LoginIntent) {
        reduce(intent)
    }

    private fun emitEffect(effect: LoginEffect) {
        viewModelScope.launch {
            _effect.emit(effect)
        }
    }

    private fun reduce(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.EmailChanged ->
                onEmailChanged(intent.email)

            is LoginIntent.PasswordChanged -> {
                onPasswordChanged(intent.password)
            }

            LoginIntent.LoginClicked -> {
                login()
            }

            LoginIntent.RegisterClicked -> {
                emitEffect(LoginEffect.NavigateToRegister)
            }

            LoginIntent.TogglePasswordVisibility -> onTogglePasswordVisibility()
        }
    }

    private fun onEmailChanged(email: String) {
        _state.update {
            it.copy(email = email)
        }
    }

    private fun onPasswordChanged(password: String) {
        _state.update {
            it.copy(password = password)
        }
    }

    private fun onTogglePasswordVisibility(){
        _state.update {
            it.copy(isPasswordVisible = !it.isPasswordVisible)
        }
    }

    private fun login() {
        _state.update {
            it.copy(isLoading = true)
        }
        viewModelScope.launch {
            loginUseCase(state.value.email, state.value.password).onSuccess {
                it?.let { user ->
                    emitEffect(LoginEffect.LoginSuccess(user))
                }
            }.onFailure {
                emitEffect(LoginEffect.LoginFailure(it.message.orEmpty()))
            }
            _state.update {
                it.copy(isLoading = false)
            }
        }
    }

}