package com.connectapp.presentation.forgot_password

sealed interface ForgotPasswordIntent {
    data class EmailChanged(val email: String) : ForgotPasswordIntent
    object SendEmailClicked : ForgotPasswordIntent
    object BackToLoginClicked : ForgotPasswordIntent
}
