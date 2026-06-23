package com.connectapp.presentation.settings

data class SettingsState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val message: String? = null,
    val showImportInfoDialog: Boolean = false,
    val showAboutDialog: Boolean = false,
    val selectedFilePath: String = ""
)
