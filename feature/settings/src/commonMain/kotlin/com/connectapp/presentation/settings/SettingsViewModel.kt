package com.connectapp.presentation.settings

import androidx.lifecycle.viewModelScope
import com.connectapp.core.mvi.MviViewModel
import com.connectapp.domain.backup.ObserveLastBackupUseCase
import com.connectapp.domain.provider.AppVersionProvider
import com.connectapp.domain.usecase.LogoutUseCase
import com.connectapp.domain.usecase.ObserveCurrentAccountUseCase
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class SettingsViewModel(
    private val logoutUseCase: LogoutUseCase,
    observeCurrentAccount: ObserveCurrentAccountUseCase,
    appVersionProvider: AppVersionProvider,
    observeLastBackup: ObserveLastBackupUseCase,
) : MviViewModel<SettingsState, SettingsIntent, SettingsEffect>(SettingsState()) {

    init {
        updateState { it.copy(appVersion = appVersionProvider.versionName) }
        observeCurrentAccount()
            .onEach { account -> updateState { it.copy(account = account) } }
            .launchIn(viewModelScope)
        observeLastBackup()
            .onEach { last -> updateState { it.copy(lastBackupAt = last) } }
            .launchIn(viewModelScope)
    }

    override fun reduce(intent: SettingsIntent) {
        when (intent) {
            SettingsIntent.LogoutClicked -> {
                logoutUseCase()
                emitEffect(SettingsEffect.NavigateToLogin)
            }
        }
    }
}
