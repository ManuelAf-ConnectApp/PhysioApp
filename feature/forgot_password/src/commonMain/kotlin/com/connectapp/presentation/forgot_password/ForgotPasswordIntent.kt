package com.connectapp.presentation.forgot_password

sealed interface ForgotPasswordIntent {
    data class EmailChanged(val value: String) : ForgotPasswordIntent {
        override fun toString(): String = "EmailChanged(***)"
    }
    data class RecoveryCodeChanged(val value: String) : ForgotPasswordIntent {
        override fun toString(): String = "RecoveryCodeChanged(***)"
    }
    data class NewPasswordChanged(val value: String) : ForgotPasswordIntent {
        override fun toString(): String = "NewPasswordChanged(***)"
    }
    data class ConfirmPasswordChanged(val value: String) : ForgotPasswordIntent {
        override fun toString(): String = "ConfirmPasswordChanged(***)"
    }
    object ResetClicked : ForgotPasswordIntent
    data class NewRecoveryCodeConfirmed(val confirmed: Boolean) : ForgotPasswordIntent
    object BackToLoginClicked : ForgotPasswordIntent
}
