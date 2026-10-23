package com.connectapp.presentation.settings

sealed interface SettingsEffect {
    data object NavigateToLogin : SettingsEffect
}
