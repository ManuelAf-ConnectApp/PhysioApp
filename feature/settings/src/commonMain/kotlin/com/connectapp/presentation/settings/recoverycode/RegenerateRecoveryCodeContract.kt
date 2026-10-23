package com.connectapp.presentation.settings.recoverycode

import org.jetbrains.compose.resources.StringResource

data class RegenerateRecoveryCodeState(
    val password: String = "",
    val isGenerating: Boolean = false,
    val errorMessage: StringResource? = null,
    /** The new code, shown once (D4, D15); the previous one no longer works. */
    val newRecoveryCode: String? = null,
    val newRecoveryCodeConfirmed: Boolean = false,
) {
    val canSubmit: Boolean get() = password.isNotEmpty() && !isGenerating

    // Never leak the password or the recovery code through logs.
    override fun toString(): String =
        "RegenerateRecoveryCodeState(isGenerating=$isGenerating, generated=${newRecoveryCode != null})"
}

sealed interface RegenerateRecoveryCodeIntent {
    data class PasswordChanged(val value: String) : RegenerateRecoveryCodeIntent {
        override fun toString(): String = "PasswordChanged(***)"
    }
    object GenerateClicked : RegenerateRecoveryCodeIntent
    data class NewRecoveryCodeConfirmed(val confirmed: Boolean) : RegenerateRecoveryCodeIntent
    object DoneClicked : RegenerateRecoveryCodeIntent
}

sealed interface RegenerateRecoveryCodeEffect {
    object Done : RegenerateRecoveryCodeEffect

    /** Too many wrong passwords: the session has ended (D14 lockout). */
    object NavigateToLogin : RegenerateRecoveryCodeEffect
}
