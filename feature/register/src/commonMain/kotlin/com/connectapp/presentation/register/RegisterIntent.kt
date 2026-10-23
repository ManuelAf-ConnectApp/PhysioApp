package com.connectapp.presentation.register

sealed interface RegisterIntent {
    data class FirstNameChanged(val value: String) : RegisterIntent
    data class LastNameChanged(val value: String) : RegisterIntent
    data class EmailChanged(val value: String) : RegisterIntent {
        override fun toString(): String = "EmailChanged(***)"
    }
    data class PasswordChanged(val value: String) : RegisterIntent {
        override fun toString(): String = "PasswordChanged(***)"
    }
    data class ConfirmPasswordChanged(val value: String) : RegisterIntent {
        override fun toString(): String = "ConfirmPasswordChanged(***)"
    }
    object RegisterClicked : RegisterIntent
    data class RecoveryCodeConfirmed(val confirmed: Boolean) : RegisterIntent
    object ContinueClicked : RegisterIntent
    object LoginClicked : RegisterIntent
}
