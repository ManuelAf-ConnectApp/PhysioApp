package com.connectapp.data.backup

import com.connectapp.core.database.PhysioBackupException
import com.connectapp.domain.backup.BackupError
import com.connectapp.domain.backup.BackupFile
import com.connectapp.domain.backup.BackupRepository
import com.connectapp.domain.policy.EpochClock
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/** Extension of backup files; the content is a SQLCipher database encrypted with the backup password. */
const val BACKUP_FILE_EXTENSION = "physiobackup"

/** The database operations a backup needs (PhysioDatabaseBackup.kt in :core:database). */
interface BackupEngine {
    fun export(password: String, createdAt: Long): ByteArray

    /** Returns the creation date stored in the backup. */
    fun restore(bytes: ByteArray, password: String, requireNoAccounts: Boolean): Long
}

/** Where the date of the last backup is kept. It belongs to the device, not to the data. */
interface LastBackupStore {
    fun read(): Long?
    fun write(epochMillis: Long)
}

/**
 * [BackupRepository] over the encrypted database (plan §21.7). Key derivation (SQLCipher's PBKDF2)
 * and file I/O run on [dispatcher].
 */
class BackupRepositoryImpl(
    private val engine: BackupEngine,
    private val store: LastBackupStore,
    private val clock: EpochClock,
    private val timeZone: () -> TimeZone = TimeZone::currentSystemDefault,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : BackupRepository {

    private val lastBackupAt = MutableStateFlow(store.read())

    override suspend fun createBackup(password: String): Result<BackupFile> = backupOperation {
        val createdAt = clock.nowMillis()
        BackupFile(fileName(createdAt), engine.export(password, createdAt), createdAt)
    }

    override suspend fun restoreBackup(bytes: ByteArray, password: String, requireNoAccounts: Boolean): Result<Unit> =
        backupOperation {
            val createdAt = engine.restore(bytes, password, requireNoAccounts)
            // The data on the device is now exactly the backup's.
            record(createdAt)
        }

    override fun markBackupSaved(createdAt: Long) = record(createdAt)

    override fun observeLastBackupAt(): Flow<Long?> = lastBackupAt

    private fun record(createdAt: Long) {
        store.write(createdAt)
        lastBackupAt.value = createdAt
    }

    /** `physioapp-copia-2026-10-21-1530.physiobackup`: a date, never personal data. */
    @OptIn(ExperimentalTime::class)
    private fun fileName(createdAt: Long): String {
        val time: LocalDateTime = Instant.fromEpochMilliseconds(createdAt).toLocalDateTime(timeZone())
        fun Int.twoDigits() = toString().padStart(2, '0')
        val date = "${time.year}-${time.month.ordinal.plus(1).twoDigits()}-${time.day.twoDigits()}"
        return "physioapp-copia-$date-${time.hour.twoDigits()}${time.minute.twoDigits()}.$BACKUP_FILE_EXTENSION"
    }

    /** Runs [block] on [dispatcher]; database failures become [BackupError]s. */
    private suspend fun <T> backupOperation(block: () -> T): Result<T> = withContext(dispatcher) {
        try {
            Result.success(block())
        } catch (error: CancellationException) {
            throw error
        } catch (error: PhysioBackupException) {
            Result.failure(error.toBackupError())
        } catch (@Suppress("TooGenericExceptionCaught") error: Exception) {
            Result.failure(BackupError.Unknown(error))
        }
    }
}

private fun PhysioBackupException.toBackupError(): BackupError = when (this) {
    is PhysioBackupException.InvalidFileOrPassword -> BackupError.WrongPasswordOrFile
    is PhysioBackupException.IncompatibleVersion -> BackupError.IncompatibleVersion(madeWithNewerApp)
    is PhysioBackupException.DeviceHasAccounts -> BackupError.DeviceHasAccounts
}
