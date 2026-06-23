package com.connectapp.presentation.settings

sealed interface SettingsIntent {
    data class SelectFilePath(val path: String) : SettingsIntent
    data class ImportDatabase(val databaseName: String) : SettingsIntent
    data class ExportDatabase(val databaseName: String) : SettingsIntent
    data object DismissImportInfoDialog : SettingsIntent
    data object ShowAboutDialog : SettingsIntent
    data object DismissAboutDialog : SettingsIntent
    data object Logout : SettingsIntent
}
