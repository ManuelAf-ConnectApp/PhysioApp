package com.connectapp.presentation.login

import org.jetbrains.compose.resources.StringResource

data class LoginState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isPasswordVisible: Boolean = false,
    /** D14: off by default. */
    val rememberSessionEnabled: Boolean = false,
    val errorMessage: StringResource? = null,
    /** Argument of [errorMessage] when it needs one (minutes left of a lockout). */
    val errorArgument: String? = null,
    /** A tablet without accounts (new or reinstalled) can bring its data back from a backup. */
    val canRestoreBackup: Boolean = false,
) {
    // Never leak the password through logs.
    override fun toString(): String = "LoginState(isLoading=$isLoading, hasError=${errorMessage != null})"
}
