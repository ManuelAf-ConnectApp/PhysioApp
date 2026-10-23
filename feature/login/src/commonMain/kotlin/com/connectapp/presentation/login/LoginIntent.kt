package com.connectapp.presentation.login

sealed interface LoginIntent {
    data class EmailChanged(val email: String) : LoginIntent {
        override fun toString(): String = "EmailChanged(***)"
    }
    data class PasswordChanged(val password: String) : LoginIntent {
        override fun toString(): String = "PasswordChanged(***)"
    }
    object LoginClicked : LoginIntent
    object RegisterClicked : LoginIntent
    object ForgotPasswordClicked : LoginIntent
    object RestoreBackupClicked : LoginIntent
    object TogglePasswordVisibility : LoginIntent
    data class RememberSessionToggled(val enabled: Boolean) : LoginIntent
}
