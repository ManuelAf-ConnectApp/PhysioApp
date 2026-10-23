package com.connectapp.data.backup.di

import com.connectapp.core.database.EncryptedPhysioDatabase
import com.connectapp.core.database.exportBackup
import com.connectapp.core.database.restoreBackup
import com.connectapp.core.storage.createDeviceBoundKVault
import com.connectapp.data.backup.BackupEngine
import com.connectapp.data.backup.BackupRepositoryImpl
import com.connectapp.data.backup.LastBackupStore
import com.connectapp.domain.backup.BackupRepository
import com.liftric.kvault.KVault
import org.koin.dsl.module

// Stable across launches: renaming it would forget the date of the last backup.
private const val BACKUP_STORE_NAME = "connectapp_backup"
private const val KEY_LAST_BACKUP_AT = "last_backup_at"

private class DatabaseBackupEngine(private val database: EncryptedPhysioDatabase) : BackupEngine {
    override fun export(password: String, createdAt: Long): ByteArray = database.exportBackup(password, createdAt)

    override fun restore(bytes: ByteArray, password: String, requireNoAccounts: Boolean): Long =
        database.restoreBackup(bytes, password, requireNoAccounts).createdAt
}

private class KVaultLastBackupStore(private val kVault: KVault) : LastBackupStore {
    override fun read(): Long? = kVault.string(KEY_LAST_BACKUP_AT)?.toLongOrNull()

    override fun write(epochMillis: Long) {
        kVault.set(KEY_LAST_BACKUP_AT, epochMillis.toString())
    }
}

/**
 * The [EncryptedPhysioDatabase] comes from composeApp's databaseModule and the
 * [com.connectapp.domain.policy.EpochClock] from :data:auth's module.
 */
val backupDataModule = module {
    single<BackupRepository> {
        BackupRepositoryImpl(
            engine = DatabaseBackupEngine(get()),
            store = KVaultLastBackupStore(createDeviceBoundKVault(BACKUP_STORE_NAME)),
            clock = get(),
        )
    }
}
