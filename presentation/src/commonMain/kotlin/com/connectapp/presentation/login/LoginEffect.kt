package com.connectapp.presentation.login

import com.connectapp.domain.model.User

sealed interface LoginEffect {
    data class ShowError(val message: String) : LoginEffect
    data class LoginSuccess(val user: User) : LoginEffect

    data class LoginFailure(val error: String) : LoginEffect
    data object NavigateToRegister : LoginEffect
    data object NavigateToForgotPassword : LoginEffect
}