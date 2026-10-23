package com.connectapp.presentation.login

sealed interface LoginEffect {
    object NavigateToHome : LoginEffect

    /** Mandatory professional profile still missing (D3). */
    object NavigateToOnboarding : LoginEffect
    object NavigateToRegister : LoginEffect
    object NavigateToForgotPassword : LoginEffect
    object NavigateToRestoreBackup : LoginEffect
}
