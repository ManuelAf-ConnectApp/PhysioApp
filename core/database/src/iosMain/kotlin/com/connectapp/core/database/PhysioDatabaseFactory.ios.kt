package com.connectapp.core.database

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import app.cash.sqldelight.driver.native.wrapConnection
import co.touchlab.sqliter.DatabaseConfiguration
import co.touchlab.sqliter.DatabaseFileContext
import co.touchlab.sqliter.interop.Logger
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSFileManager
import platform.Foundation.NSFileProtectionCompleteUnlessOpen
import platform.Foundation.NSFileProtectionKey
import platform.Foundation.NSURL
import platform.Foundation.NSURLIsExcludedFromBackupKey

// SQLiter issues `PRAGMA key = '<passphrase>'` on every new connection. It only encrypts when the
// app links SQLCipher (iosApp: Swift Package "SQLCipher"); openPhysioDatabase() verifies that.
actual fun createEncryptedSqlDriver(
    schema: SqlSchema<QueryResult.Value<Unit>>,
    databaseName: String,
    passphrase: String,
): SqlDriver = NativeSqliteDriver(
    DatabaseConfiguration(
        name = databaseName,
        version = schema.version.toInt(),
        create = { connection -> wrapConnection(connection) { schema.create(it) } },
        upgrade = { connection, oldVersion, newVersion ->
            wrapConnection(connection) { schema.migrate(it, oldVersion.toLong(), newVersion.toLong()) }
        },
        extendedConfig = DatabaseConfiguration.Extended(foreignKeyConstraints = true),
        encryptionConfig = DatabaseConfiguration.Encryption(key = passphrase),
        loggingConfig = DatabaseConfiguration.Logging(logger = SilentSqliteLogger),
    )
)

/**
 * SQLiter's default logger prints failing SQL and stack traces to the console. PhysioApp never
 * writes database internals to logs (no clinical data in logs); errors still
 * reach callers as exceptions.
 */
internal object SilentSqliteLogger : Logger {
    override fun trace(message: String) = Unit
    override val vActive: Boolean = false
    override fun vWrite(message: String) = Unit
    override val eActive: Boolean = false
    override fun eWrite(message: String, exception: Throwable?) = Unit
}

/**
 * Same stance as Android's `allowBackup="false"` (A5): the database folder is excluded from
 * iCloud/computer backups — a restored copy could not be opened anyway, because its key stays in
 * this device's Keychain (WhenUnlockedThisDeviceOnly). The app's own encrypted backups (D20,
 * PhysioDatabaseBackup.kt) cover moving the data to another device. Its files
 * also get CompleteUnlessOpen protection: unreadable while the iPad is locked, except a file the
 * app already has open, so a lock mid-write does not break SQLite. New files (journal, WAL)
 * inherit the folder's protection class.
 */
@OptIn(ExperimentalForeignApi::class)
internal actual fun protectDatabaseFiles(databaseName: String) {
    val directory = DatabaseFileContext.databasePath(databaseName, null).substringBeforeLast('/')
    val manager = NSFileManager.defaultManager
    val protection = mapOf<Any?, Any?>(NSFileProtectionKey to NSFileProtectionCompleteUnlessOpen)
    manager.setAttributes(protection, ofItemAtPath = directory, error = null)
    manager.contentsOfDirectoryAtPath(directory, error = null)?.forEach { file ->
        manager.setAttributes(protection, ofItemAtPath = "$directory/$file", error = null)
    }
    NSURL.fileURLWithPath(directory, isDirectory = true)
        .setResourceValue(true, forKey = NSURLIsExcludedFromBackupKey, error = null)
}
