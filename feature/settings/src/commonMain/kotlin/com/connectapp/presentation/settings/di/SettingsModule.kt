package com.connectapp.presentation.settings.di

import com.connectapp.presentation.settings.SettingsViewModel
import com.connectapp.presentation.settings.password.ChangePasswordViewModel
import com.connectapp.presentation.settings.recoverycode.RegenerateRecoveryCodeViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val settingsModule = module {
    viewModelOf(::SettingsViewModel)
    viewModelOf(::ChangePasswordViewModel)
    viewModelOf(::RegenerateRecoveryCodeViewModel)
}
