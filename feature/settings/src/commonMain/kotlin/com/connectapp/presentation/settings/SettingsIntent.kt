package com.connectapp.presentation.settings

/**
 * Settings scope (D15, confirmed 2026-10-21; licences added at the owner's request the same day):
 * log out, About (version), open-source licences, change password and regenerate the recovery
 * code. The last three are screens of their own, opened by navigation.
 */
sealed interface SettingsIntent {
    data object LogoutClicked : SettingsIntent
}
