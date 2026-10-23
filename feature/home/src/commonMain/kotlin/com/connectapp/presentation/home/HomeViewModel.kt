package com.connectapp.presentation.home

import androidx.lifecycle.viewModelScope
import com.connectapp.core.mvi.MviViewModel
import com.connectapp.domain.backup.ObserveBackupReminderUseCase
import com.connectapp.domain.usecase.ObserveCurrentAccountUseCase
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class HomeViewModel(
    observeCurrentAccount: ObserveCurrentAccountUseCase,
    observeBackupReminder: ObserveBackupReminderUseCase,
) : MviViewModel<HomeState, HomeIntent, HomeEffect>(HomeState()) {

    init {
        observeCurrentAccount()
            .onEach { account -> updateState { it.copy(firstName = account?.firstName) } }
            .launchIn(viewModelScope)
        observeBackupReminder()
            .onEach { reminder -> updateState { it.copy(backupReminder = reminder) } }
            .launchIn(viewModelScope)
    }

    override fun reduce(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.DestinationClicked -> emitEffect(HomeEffect.NavigateTo(intent.destination))
            HomeIntent.BackupReminderClicked -> emitEffect(HomeEffect.OpenBackup)
        }
    }
}
