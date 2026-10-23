package com.connectapp.presentation.backup.restore

import com.connectapp.domain.backup.BackupField
import com.connectapp.domain.backup.BackupValidation
import org.jetbrains.compose.resources.StringResource

data class RestoreBackupState(
    /** From the sign-in screen there is no session, so no session password is asked. */
    val fromSignIn: Boolean,
    val fileName: String? = null,
    val fileBytes: ByteArray? = null,
    val password: String = "",
    val sessionPassword: String = "",
    val showConfirmation: Boolean = false,
    val isRestoring: Boolean = false,
    val errorMessage: StringResource? = null,
) {
    private val sessionPasswordOrNull: String? get() = if (fromSignIn) null else sessionPassword

    val invalidFields: Set<BackupField>
        get() = BackupValidation.restoreErrors(fileBytes != null, password, sessionPasswordOrNull)

    val canSubmit: Boolean get() = invalidFields.isEmpty() && !isRestoring

    // Never leak passwords or the backup content through logs; ByteArray also breaks data class equality.
    override fun toString(): String =
        "RestoreBackupState(fromSignIn=$fromSignIn, hasFile=${fileBytes != null}, isRestoring=$isRestoring)"

    override fun equals(other: Any?): Boolean = other is RestoreBackupState &&
        fromSignIn == other.fromSignIn &&
        fileName == other.fileName &&
        fileBytes.contentEquals(other.fileBytes) &&
        password == other.password &&
        sessionPassword == other.sessionPassword &&
        showConfirmation == other.showConfirmation &&
        isRestoring == other.isRestoring &&
        errorMessage == other.errorMessage

    override fun hashCode(): Int = toString().hashCode()
}

sealed interface RestoreBackupIntent {
    /** The user picked a file; the content is only checked when restoring. */
    class FilePicked(val fileName: String, val bytes: ByteArray) : RestoreBackupIntent {
        override fun toString(): String = "FilePicked(size=${bytes.size})"
    }
    data class PasswordChanged(val value: String) : RestoreBackupIntent {
        override fun toString(): String = "PasswordChanged(***)"
    }
    data class SessionPasswordChanged(val value: String) : RestoreBackupIntent {
        override fun toString(): String = "SessionPasswordChanged(***)"
    }
    object RestoreClicked : RestoreBackupIntent
    object ConfirmationDismissed : RestoreBackupIntent
    object RestoreConfirmed : RestoreBackupIntent
}

sealed interface RestoreBackupEffect {
    /** The data is restored and any session has ended. */
    object Restored : RestoreBackupEffect

    /** Too many wrong session passwords: the session has ended (D14 lockout). */
    object NavigateToLogin : RestoreBackupEffect
}
