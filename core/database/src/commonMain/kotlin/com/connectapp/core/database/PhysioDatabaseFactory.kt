package com.connectapp.core.database

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import com.connectapp.core.database.physio.PhysioDatabase

/** SQLite file name of PhysioApp's single local database. Must stay stable across releases. */
const val PHYSIO_DATABASE_NAME = "physioapp.db"

/** Minimum passphrase length accepted by [openPhysioDatabase] (64 hex chars = 256 random bits). */
const val MIN_PASSPHRASE_LENGTH = 64

/**
 * Creates a SQLCipher-encrypted [SqlDriver] with foreign-key enforcement enabled on every
 * connection. [passphrase] is used verbatim as the SQLCipher passphrase on both platforms.
 */
expect fun createEncryptedSqlDriver(
    schema: SqlSchema<QueryResult.Value<Unit>>,
    databaseName: String,
    passphrase: String,
): SqlDriver

/**
 * Keeps the files of [databaseName] out of device backups and, where the platform supports it,
 * unreadable while the device is locked. Android needs nothing here: the app already opts out of
 * backups (`allowBackup="false"` and data extraction rules, A5). Best effort: the database is
 * SQLCipher-encrypted either way.
 */
internal expect fun protectDatabaseFiles(databaseName: String)

/** Thrown when the linked SQLite library is not SQLCipher, so data would be stored in clear. */
class DatabaseNotEncryptedException :
    IllegalStateException("The linked SQLite library is not SQLCipher: refusing to store data unencrypted")

/** SQLCipher's version string, or null when the driver is backed by plain SQLite. */
fun SqlDriver.cipherVersion(): String? =
    executeQuery(
        identifier = null,
        sql = "PRAGMA cipher_version",
        mapper = { cursor -> QueryResult.Value(if (cursor.next().value) cursor.getString(0) else null) },
        parameters = 0,
    ).value

/**
 * Opens (creating it on first use) PhysioApp's encrypted database
 * (docs/00-auditoria-y-plan.md §6, D7).
 *
 * Fails closed: if the runtime is not SQLCipher — e.g. an iOS build that accidentally links the
 * system libsqlite3, where `PRAGMA key` is silently ignored — it throws
 * [DatabaseNotEncryptedException] instead of writing health data in clear. A wrong [passphrase]
 * for an existing file makes the driver throw; the file is never deleted or recreated here.
 */
fun openPhysioDatabase(passphrase: String, databaseName: String = PHYSIO_DATABASE_NAME): EncryptedPhysioDatabase {
    require(passphrase.length >= MIN_PASSPHRASE_LENGTH) {
        "The database passphrase must have at least $MIN_PASSPHRASE_LENGTH characters"
    }
    val driver = createEncryptedSqlDriver(PhysioDatabase.Schema, databaseName, passphrase)
    val version = runCatching { driver.cipherVersion() }
        .onFailure { driver.close() }
        .getOrThrow()
    if (version.isNullOrBlank()) {
        driver.close()
        throw DatabaseNotEncryptedException()
    }
    protectDatabaseFiles(databaseName)
    return EncryptedPhysioDatabase(PhysioDatabase(driver), driver, version, databaseName, passphrase)
}

/** An open, verified-encrypted [PhysioDatabase]. The app keeps one for its whole lifetime. */
class EncryptedPhysioDatabase internal constructor(
    val database: PhysioDatabase,
    internal val driver: SqlDriver,
    /** SQLCipher version reported by the runtime, e.g. "4.19.0 community". */
    val cipherVersion: String,
    // Backups open their own connection to the same file (PhysioDatabaseBackup.kt).
    internal val databaseName: String,
    internal val passphrase: String,
) : AutoCloseable {
    override fun close() = driver.close()

    override fun toString(): String = "EncryptedPhysioDatabase(cipherVersion=$cipherVersion)"
}
