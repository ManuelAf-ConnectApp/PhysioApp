package com.connectapp.domain.backup

import com.connectapp.domain.shared.validation.PasswordPolicy
import kotlinx.coroutines.flow.Flow

/**
 * A backup of the whole database, encrypted with its own password, ready to be saved outside the
 * app (plan §21.7). [fileName] never carries personal data.
 */
class BackupFile(val fileName: String, val bytes: ByteArray, val createdAt: Long) {
    override fun toString(): String = "BackupFile(fileName=$fileName, size=${bytes.size}, createdAt=$createdAt)"
}

/** Only type of failure that [BackupRepository]'s `Result<T>` may carry. */
sealed class BackupError : Exception() {
    /** Input that breaks a validation rule reached the use case (the UI should prevent it). */
    object InvalidInput : BackupError()

    /** Wrong backup password, or the file is not a PhysioApp backup: they cannot be told apart. */
    object WrongPasswordOrFile : BackupError()

    /** The backup was made with a different database version of the app. */
    data class IncompatibleVersion(val madeWithNewerApp: Boolean) : BackupError()

    /** A restore from the sign-in screen found accounts on the device. */
    object DeviceHasAccounts : BackupError()

    data class Unknown(override val cause: Throwable) : BackupError()
}

/**
 * Backups of the encrypted database. The copy uses a password chosen when it is made, never this
 * device's key, so it can be restored on another tablet; the password is not stored anywhere.
 */
interface BackupRepository {
    /** Writes a complete copy of the database encrypted with [password]. */
    suspend fun createBackup(password: String): Result<BackupFile>

    /**
     * Replaces all the data on this device with the backup in [bytes], atomically. With
     * [requireNoAccounts] it fails with [BackupError.DeviceHasAccounts] if the device has accounts.
     * A successful restore also counts as the last backup, with the backup's own date.
     */
    suspend fun restoreBackup(bytes: ByteArray, password: String, requireNoAccounts: Boolean): Result<Unit>

    /** Records that the backup made at [createdAt] has been saved outside the app. */
    fun markBackupSaved(createdAt: Long)

    /** Epoch millis of the last backup saved from (or restored on) this device, or null if none. */
    fun observeLastBackupAt(): Flow<Long?>
}

/** Fields of the backup forms that can carry a validation error. */
enum class BackupField {
    PASSWORD,
    PASSWORD_CONFIRMATION,
    SESSION_PASSWORD,
    FILE,
}

/** Rules of the backup forms, shared by the use cases and the screens. */
object BackupValidation {
    /** The backup password follows the account password policy (D16), typed twice. */
    fun createErrors(password: String, confirmation: String): Set<BackupField> = buildSet {
        if (!PasswordPolicy.isValid(password)) add(BackupField.PASSWORD)
        if (!PasswordPolicy.isValidConfirmation(password, confirmation)) add(BackupField.PASSWORD_CONFIRMATION)
    }

    /**
     * A file and its password; [sessionPassword] is null on the sign-in screen, where there is no
     * session, and must be typed everywhere else.
     */
    fun restoreErrors(hasFile: Boolean, password: String, sessionPassword: String?): Set<BackupField> = buildSet {
        if (!hasFile) add(BackupField.FILE)
        if (password.isEmpty()) add(BackupField.PASSWORD)
        if (sessionPassword != null && sessionPassword.isEmpty()) add(BackupField.SESSION_PASSWORD)
    }
}

/** When Home reminds to make a backup (plan §21.7). */
object BackupPolicy {
    const val REMINDER_INTERVAL_MILLIS: Long = 7 * 24 * 60 * 60 * 1000L

    fun isReminderDue(lastBackupAt: Long?, now: Long): Boolean =
        lastBackupAt == null || now - lastBackupAt >= REMINDER_INTERVAL_MILLIS
}
