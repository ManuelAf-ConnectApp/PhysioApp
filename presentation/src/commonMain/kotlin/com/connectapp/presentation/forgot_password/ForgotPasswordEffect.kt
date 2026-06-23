package com.connectapp.presentation.forgot_password

sealed interface ForgotPasswordEffect {
    object NavigateToLogin : ForgotPasswordEffect
}
