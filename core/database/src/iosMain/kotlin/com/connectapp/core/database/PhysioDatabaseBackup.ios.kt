@file:OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)

package com.connectapp.core.database

import co.touchlab.sqliter.DatabaseConfiguration
import co.touchlab.sqliter.DatabaseConnection
import co.touchlab.sqliter.createDatabaseManager
import co.touchlab.sqliter.getStringOrNull
import co.touchlab.sqliter.withStatement
import co.touchlab.sqliter.withTransaction
import com.connectapp.core.database.physio.PhysioDatabase
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSDataWritingAtomic
import platform.Foundation.NSDataWritingFileProtectionComplete
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.create
import platform.Foundation.dataWithContentsOfFile
import platform.Foundation.writeToFile
import platform.posix.memcpy
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/** Subfolder of the app's temporary directory where backups are written and read. */
private const val BACKUP_WORK_FOLDER = "backup"

// Same file, key and settings as createEncryptedSqlDriver; the schema already exists, so the
// create/upgrade callbacks never run on this connection.
internal actual fun openBackupConnection(databaseName: String, passphrase: String): BackupConnection {
    val manager = createDatabaseManager(
        DatabaseConfiguration(
            name = databaseName,
            version = PhysioDatabase.Schema.version.toInt(),
            create = {},
            extendedConfig = DatabaseConfiguration.Extended(foreignKeyConstraints = true),
            encryptionConfig = DatabaseConfiguration.Encryption(key = passphrase),
            loggingConfig = DatabaseConfiguration.Logging(logger = SilentSqliteLogger),
        )
    )
    return IosBackupConnection(manager.createSingleThreadedConnection())
}

private class IosBackupConnection(private val connection: DatabaseConnection) : BackupConnection {
    override fun execute(sql: String, vararg args: String) = connection.withStatement(sql) {
        args.forEachIndexed { index, value -> bindString(index + 1, value) }
        execute()
    }

    override fun query(sql: String): List<List<String?>> = connection.withStatement(sql) {
        val cursor = query()
        buildList {
            while (cursor.next()) add((0 until cursor.columnCount).map { cursor.getStringOrNull(it) })
        }
    }

    override fun transaction(block: () -> Unit) = connection.withTransaction { block() }

    override fun close() = connection.close()
}

@OptIn(ExperimentalUuidApi::class)
internal actual fun createBackupWorkFolder(): String {
    val root = NSTemporaryDirectory().trimEnd('/') + "/" + BACKUP_WORK_FOLDER
    val manager = NSFileManager.defaultManager
    // Leftovers of an operation interrupted by the app being killed.
    manager.removeItemAtPath(root, error = null)
    val folder = "$root/${Uuid.random()}"
    manager.createDirectoryAtPath(folder, withIntermediateDirectories = true, attributes = null, error = null)
    return folder
}

internal actual fun deleteBackupWorkFolder(path: String) {
    NSFileManager.defaultManager.removeItemAtPath(path, error = null)
}

internal actual fun writeWorkFile(path: String, bytes: ByteArray) {
    val data = if (bytes.isEmpty()) {
        NSData()
    } else {
        bytes.usePinned { NSData.create(bytes = it.addressOf(0), length = bytes.size.convert()) }
    }
    check(data.writeToFile(path, options = NSDataWritingFileProtectionComplete or NSDataWritingAtomic, error = null)) {
        "Could not write the backup work file"
    }
}

internal actual fun readWorkFile(path: String): ByteArray {
    val data = checkNotNull(NSData.dataWithContentsOfFile(path)) { "Could not read the backup work file" }
    val bytes = ByteArray(data.length.toInt())
    if (bytes.isNotEmpty()) bytes.usePinned { memcpy(it.addressOf(0), data.bytes, data.length) }
    return bytes
}
