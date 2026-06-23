package com.connectapp.presentation.register

sealed interface RegisterIntent {
    data class FirstNameChanged(val firstName: String) : RegisterIntent
    data class LastNameChanged(val lastName: String) : RegisterIntent
    data class EmailChanged(val email: String) : RegisterIntent
    data class PasswordChanged(val password: String) : RegisterIntent
    data class ConfirmPasswordChanged(val confirmPassword: String) : RegisterIntent
    object RegisterClicked : RegisterIntent
    object LoginClicked : RegisterIntent
    data object TogglePasswordVisibility : RegisterIntent

    data object ToggleConfirmPasswordVisibility : RegisterIntent
}
