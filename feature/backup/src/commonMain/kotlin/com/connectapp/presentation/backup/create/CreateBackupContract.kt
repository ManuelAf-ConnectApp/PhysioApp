package com.connectapp.presentation.backup.create

import com.connectapp.commonresources.export.ExportedDocument
import com.connectapp.domain.backup.BackupField
import com.connectapp.domain.backup.BackupValidation
import org.jetbrains.compose.resources.StringResource

data class CreateBackupState(
    val password: String = "",
    val confirmation: String = "",
    val isCreating: Boolean = false,
    /** The file is in the system "save to" picker. */
    val isSaving: Boolean = false,
    val errorMessage: StringResource? = null,
    /** The last backup was saved during this visit. */
    val saved: Boolean = false,
    /** Epoch millis of the last backup made on this device, or null if none. */
    val lastBackupAt: Long? = null,
) {
    val invalidFields: Set<BackupField> get() = BackupValidation.createErrors(password, confirmation)

    val canSubmit: Boolean get() = invalidFields.isEmpty() && !isCreating && !isSaving

    // Never leak the backup password through logs.
    override fun toString(): String =
        "CreateBackupState(isCreating=$isCreating, isSaving=$isSaving, saved=$saved, lastBackupAt=$lastBackupAt)"
}

sealed interface CreateBackupIntent {
    data class PasswordChanged(val value: String) : CreateBackupIntent {
        override fun toString(): String = "PasswordChanged(***)"
    }
    data class ConfirmationChanged(val value: String) : CreateBackupIntent {
        override fun toString(): String = "ConfirmationChanged(***)"
    }
    object CreateClicked : CreateBackupIntent

    /** The save picker closed; [saved] is false when the user cancelled or writing failed. */
    data class SaveFinished(val saved: Boolean) : CreateBackupIntent
}

sealed interface CreateBackupEffect {
    /** Hand the encrypted file to the system "save to" picker. */
    data class SaveFile(val document: ExportedDocument) : CreateBackupEffect
}
