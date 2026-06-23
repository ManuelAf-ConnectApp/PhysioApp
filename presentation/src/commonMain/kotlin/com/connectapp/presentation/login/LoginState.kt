package com.connectapp.presentation.login

data class LoginState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val isPasswordVisible: Boolean = false,
    val errorMessage: String? = null
)
