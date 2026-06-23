package com.connectapp.presentation.settings

sealed interface SettingsEffect {
    data class ShowSnackbar(val message: String) : SettingsEffect
    data object LogoutSuccess : SettingsEffect
}
