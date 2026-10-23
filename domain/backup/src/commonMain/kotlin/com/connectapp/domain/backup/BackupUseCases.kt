package com.connectapp.domain.backup

import com.connectapp.domain.policy.EpochClock
import com.connectapp.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Settings → create backup: a password that follows the policy, typed twice. */
class CreateBackupUseCase(
    private val repository: BackupRepository,
) {
    suspend operator fun invoke(password: String, confirmation: String): Result<BackupFile> =
        if (BackupValidation.createErrors(password, confirmation).isNotEmpty()) {
            Result.failure(BackupError.InvalidInput)
        } else {
            repository.createBackup(password)
        }
}

/** The user saved the backup somewhere outside the app (the save picker did not report a cancel). */
class MarkBackupSavedUseCase(
    private val repository: BackupRepository,
) {
    operator fun invoke(createdAt: Long) = repository.markBackupSaved(createdAt)
}

/**
 * Restores a backup over all the data on this device.
 *
 * - From the sign-in screen ([sessionPassword] null): only on a device without accounts, such as a
 *   new or reinstalled tablet.
 * - From Settings: the password of the session is checked first (failures count towards the
 *   lockout, [com.connectapp.domain.model.AuthError]), and after the restore the session ends,
 *   since the account may no longer exist or have another password.
 *
 * Failures are [BackupError]s, or [com.connectapp.domain.model.AuthError]s from the password check.
 */
class RestoreBackupUseCase(
    private val repository: BackupRepository,
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(bytes: ByteArray?, password: String, sessionPassword: String?): Result<Unit> = when {
        bytes == null || BackupValidation.restoreErrors(true, password, sessionPassword).isNotEmpty() ->
            Result.failure(BackupError.InvalidInput)
        sessionPassword == null -> repository.restoreBackup(bytes, password, requireNoAccounts = true)
        else -> authRepository.verifyPassword(sessionPassword).fold(
            onSuccess = {
                repository.restoreBackup(bytes, password, requireNoAccounts = false)
                    .onSuccess { authRepository.logout() }
            },
            onFailure = { Result.failure(it) },
        )
    }
}

class ObserveLastBackupUseCase(
    private val repository: BackupRepository,
) {
    operator fun invoke(): Flow<Long?> = repository.observeLastBackupAt()
}

/**
 * Home's reminder: emits the date of the last backup (null if there has never been one) while a
 * backup is due ([BackupPolicy]), and [NoReminder] otherwise.
 */
class ObserveBackupReminderUseCase(
    private val repository: BackupRepository,
    private val clock: EpochClock,
) {
    operator fun invoke(): Flow<BackupReminder> = repository.observeLastBackupAt().map { last ->
        if (BackupPolicy.isReminderDue(last, clock.nowMillis())) BackupReminder.Due(last) else BackupReminder.NoReminder
    }
}

sealed interface BackupReminder {
    object NoReminder : BackupReminder

    /** A backup is due; [lastBackupAt] is null when none has ever been made. */
    data class Due(val lastBackupAt: Long?) : BackupReminder
}
