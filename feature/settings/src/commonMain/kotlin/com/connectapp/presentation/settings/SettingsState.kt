package com.connectapp.presentation.settings

import com.connectapp.domain.model.Account

data class SettingsState(
    val account: Account? = null,
    val appVersion: String = "",
    /** Epoch millis of the last backup made on this device, or null if none. */
    val lastBackupAt: Long? = null,
) {
    val userName: String get() = account?.let { "${it.firstName} ${it.lastName}" }.orEmpty()
    val userEmail: String get() = account?.email.orEmpty()
}
