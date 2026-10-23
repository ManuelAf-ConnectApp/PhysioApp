package com.connectapp.presentation.backup.restore

import androidx.lifecycle.viewModelScope
import com.connectapp.commonresources.error_backup_device_has_accounts
import com.connectapp.commonresources.error_backup_newer
import com.connectapp.commonresources.error_backup_older
import com.connectapp.commonresources.error_backup_wrong_password
import com.connectapp.commonresources.error_unexpected
import com.connectapp.commonresources.error_wrong_password
import com.connectapp.core.mvi.MviViewModel
import com.connectapp.domain.backup.BackupError
import com.connectapp.domain.backup.RestoreBackupUseCase
import com.connectapp.domain.model.AuthError
import kotlinx.coroutines.launch

/**
 * Restore a backup (plan §21.7), from Settings or, on a tablet without accounts, from the sign-in
 * screen. A confirmation dialog comes first, since every row on the device is replaced.
 */
class RestoreBackupViewModel(
    fromSignIn: Boolean,
    private val restoreBackupUseCase: RestoreBackupUseCase,
) : MviViewModel<RestoreBackupState, RestoreBackupIntent, RestoreBackupEffect>(RestoreBackupState(fromSignIn)) {

    override fun reduce(intent: RestoreBackupIntent) {
        when (intent) {
            is RestoreBackupIntent.FilePicked -> updateState {
                it.copy(fileName = intent.fileName, fileBytes = intent.bytes, password = "", errorMessage = null)
            }
            is RestoreBackupIntent.PasswordChanged ->
                updateState { it.copy(password = intent.value, errorMessage = null) }
            is RestoreBackupIntent.SessionPasswordChanged ->
                updateState { it.copy(sessionPassword = intent.value, errorMessage = null) }
            RestoreBackupIntent.RestoreClicked ->
                if (state.value.canSubmit) updateState { it.copy(showConfirmation = true) }
            RestoreBackupIntent.ConfirmationDismissed -> updateState { it.copy(showConfirmation = false) }
            RestoreBackupIntent.RestoreConfirmed -> restore()
        }
    }

    private fun restore() {
        val form = state.value
        updateState { it.copy(showConfirmation = false) }
        if (!form.canSubmit) return
        updateState { it.copy(isRestoring = true, errorMessage = null) }
        viewModelScope.launch {
            restoreBackupUseCase(
                bytes = form.fileBytes,
                password = form.password,
                sessionPassword = if (form.fromSignIn) null else form.sessionPassword,
            )
                .onSuccess {
                    updateState { RestoreBackupState(form.fromSignIn) }
                    emitEffect(RestoreBackupEffect.Restored)
                }
                .onFailure(::showFailure)
        }
    }

    private fun showFailure(error: Throwable) {
        when (error) {
            is AuthError.AccountLocked, AuthError.NotAuthenticated -> {
                updateState { RestoreBackupState(it.fromSignIn) }
                emitEffect(RestoreBackupEffect.NavigateToLogin)
            }
            AuthError.InvalidCredentials -> updateState {
                it.copy(isRestoring = false, sessionPassword = "", errorMessage = error_wrong_password)
            }
            BackupError.WrongPasswordOrFile -> updateState {
                it.copy(isRestoring = false, password = "", errorMessage = error_backup_wrong_password)
            }
            is BackupError.IncompatibleVersion -> updateState {
                it.copy(
                    isRestoring = false,
                    errorMessage = if (error.madeWithNewerApp) error_backup_newer else error_backup_older,
                )
            }
            BackupError.DeviceHasAccounts -> updateState {
                it.copy(isRestoring = false, errorMessage = error_backup_device_has_accounts)
            }
            else -> updateState { it.copy(isRestoring = false, errorMessage = error_unexpected) }
        }
    }
}
