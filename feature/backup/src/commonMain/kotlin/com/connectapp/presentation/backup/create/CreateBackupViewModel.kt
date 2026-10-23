package com.connectapp.presentation.backup.create

import androidx.lifecycle.viewModelScope
import com.connectapp.commonresources.backup_not_saved
import com.connectapp.commonresources.error_unexpected
import com.connectapp.commonresources.export.BINARY_MIME_TYPE
import com.connectapp.commonresources.export.ExportedDocument
import com.connectapp.core.mvi.MviViewModel
import com.connectapp.domain.backup.CreateBackupUseCase
import com.connectapp.domain.backup.MarkBackupSavedUseCase
import com.connectapp.domain.backup.ObserveLastBackupUseCase
import kotlinx.coroutines.launch

/**
 * Settings → create backup (plan §21.7): the user picks a password, the app writes the encrypted
 * copy and hands it to the system "save to" picker. It only counts as the last backup once the
 * picker reports the file as saved.
 */
class CreateBackupViewModel(
    private val createBackupUseCase: CreateBackupUseCase,
    private val markBackupSavedUseCase: MarkBackupSavedUseCase,
    observeLastBackupUseCase: ObserveLastBackupUseCase,
) : MviViewModel<CreateBackupState, CreateBackupIntent, CreateBackupEffect>(CreateBackupState()) {

    private var pendingCreatedAt: Long? = null

    init {
        viewModelScope.launch {
            observeLastBackupUseCase().collect { last -> updateState { it.copy(lastBackupAt = last) } }
        }
    }

    override fun reduce(intent: CreateBackupIntent) {
        when (intent) {
            is CreateBackupIntent.PasswordChanged ->
                updateState { it.copy(password = intent.value, errorMessage = null, saved = false) }
            is CreateBackupIntent.ConfirmationChanged ->
                updateState { it.copy(confirmation = intent.value, errorMessage = null, saved = false) }
            CreateBackupIntent.CreateClicked -> create()
            is CreateBackupIntent.SaveFinished -> saveFinished(intent.saved)
        }
    }

    private fun create() {
        val form = state.value
        if (!form.canSubmit) return
        updateState { it.copy(isCreating = true, errorMessage = null, saved = false) }
        viewModelScope.launch {
            createBackupUseCase(form.password, form.confirmation)
                .onSuccess { file ->
                    pendingCreatedAt = file.createdAt
                    updateState { it.copy(isCreating = false, isSaving = true) }
                    emitEffect(
                        CreateBackupEffect.SaveFile(ExportedDocument(file.fileName, file.bytes, BINARY_MIME_TYPE)),
                    )
                }
                .onFailure { updateState { it.copy(isCreating = false, errorMessage = error_unexpected) } }
        }
    }

    private fun saveFinished(saved: Boolean) {
        val createdAt = pendingCreatedAt
        pendingCreatedAt = null
        if (saved && createdAt != null) {
            markBackupSavedUseCase(createdAt)
            updateState { it.copy(password = "", confirmation = "", isSaving = false, saved = true) }
        } else {
            updateState { it.copy(isSaving = false, errorMessage = backup_not_saved) }
        }
    }
}
