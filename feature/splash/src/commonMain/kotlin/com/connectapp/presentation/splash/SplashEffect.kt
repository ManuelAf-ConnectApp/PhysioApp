package com.connectapp.presentation.splash

sealed interface SplashEffect {
    object NavigateToLogin : SplashEffect

    /** A restored session whose mandatory professional profile is still missing (D3). */
    object NavigateToOnboarding : SplashEffect
    object NavigateToHome : SplashEffect
}
