package com.connectapp.presentation.settings.fake

import com.connectapp.domain.backup.BackupFile
import com.connectapp.domain.backup.BackupRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal class FakeBackupRepository(
    var createResult: Result<BackupFile> = Result.success(BackupFile("physioapp.physiobackup", byteArrayOf(1), 10L)),
    var restoreResult: Result<Unit> = Result.success(Unit),
) : BackupRepository {
    val lastBackupAt = MutableStateFlow<Long?>(null)
    var createCalls = 0
        private set
    var lastRestore: Triple<ByteArray, String, Boolean>? = null
        private set

    override suspend fun createBackup(password: String): Result<BackupFile> {
        createCalls++
        return createResult
    }

    override suspend fun restoreBackup(bytes: ByteArray, password: String, requireNoAccounts: Boolean): Result<Unit> {
        lastRestore = Triple(bytes, password, requireNoAccounts)
        return restoreResult
    }

    override fun markBackupSaved(createdAt: Long) {
        lastBackupAt.value = createdAt
    }

    override fun observeLastBackupAt(): Flow<Long?> = lastBackupAt
}
