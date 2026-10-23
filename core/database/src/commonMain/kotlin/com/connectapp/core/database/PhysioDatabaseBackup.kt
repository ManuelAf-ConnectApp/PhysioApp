package com.connectapp.core.database

import com.connectapp.core.database.physio.PhysioDatabase

/** Version of the backup file layout below; a restore refuses any other. */
private const val BACKUP_FORMAT = 1L

/** Written into every backup, so a restore can tell a PhysioApp backup from any other database. */
private const val BACKUP_APP_ID = "com.connectapp.physioapp"

/** Table added to the copy only: it never exists in the app's own database. */
private const val BACKUP_INFO_TABLE = "physioapp_backup"

/** Schema name the backup file is attached under while it is written or read. */
private const val BACKUP_SCHEMA = "backup"

/** What a restored backup says about itself. */
class PhysioBackupInfo(
    /** Epoch millis when the backup was created. */
    val createdAt: Long,
)

/** Why a backup could not be restored. Messages never carry passwords or data. */
sealed class PhysioBackupException(message: String) : Exception(message) {
    /** Wrong password, or the file is not a PhysioApp backup (SQLCipher cannot tell them apart). */
    class InvalidFileOrPassword : PhysioBackupException("Wrong password or not a PhysioApp backup")

    /** The backup was made with a different database version of the app. */
    class IncompatibleVersion(val madeWithNewerApp: Boolean) :
        PhysioBackupException("The backup was made with a different version of the app")

    /** A restore from the sign-in screen is only allowed on a device without accounts. */
    class DeviceHasAccounts : PhysioBackupException("This device already has accounts")
}

/**
 * Writes a complete copy of the database to a new SQLCipher file encrypted with [password]
 * instead of this device's key, so it can be restored on any device that knows the password, and
 * returns its bytes. SQLCipher derives the key from [password] (PBKDF2-HMAC-SHA512, 256,000
 * iterations, random salt per file) and authenticates every page.
 *
 * The copy only exists in the app's private temporary folder while it is written and read back.
 */
fun EncryptedPhysioDatabase.exportBackup(password: String, createdAt: Long): ByteArray =
    withBackupWorkFile("export") { path ->
        openBackupConnection(databaseName, passphrase).use { connection ->
            connection.execute("ATTACH DATABASE ? AS $BACKUP_SCHEMA KEY ?", path, password)
            try {
                connection.query("SELECT sqlcipher_export('$BACKUP_SCHEMA')")
                connection.execute(
                    "CREATE TABLE $BACKUP_SCHEMA.$BACKUP_INFO_TABLE (format INTEGER NOT NULL, " +
                        "app TEXT NOT NULL, schema_version INTEGER NOT NULL, created_at INTEGER NOT NULL)",
                )
                connection.execute(
                    "INSERT INTO $BACKUP_SCHEMA.$BACKUP_INFO_TABLE VALUES (?, ?, ?, ?)",
                    BACKUP_FORMAT.toString(),
                    BACKUP_APP_ID,
                    PhysioDatabase.Schema.version.toString(),
                    createdAt.toString(),
                )
            } finally {
                connection.execute("DETACH DATABASE $BACKUP_SCHEMA")
            }
        }
        readWorkFile(path)
    }

/**
 * Replaces every row of the database with the rows of the backup in [bytes], in one transaction:
 * either the whole backup is restored or nothing changes. The device keeps its own key; only the
 * data is replaced. With [requireNoAccounts] (restore from the sign-in screen) it refuses to run
 * when the device already has accounts.
 *
 * Fails with [PhysioBackupException] when the password is wrong, the file is not a PhysioApp
 * backup or it comes from a different database version.
 */
fun EncryptedPhysioDatabase.restoreBackup(
    bytes: ByteArray,
    password: String,
    requireNoAccounts: Boolean,
): PhysioBackupInfo {
    val (info, tables) = withBackupWorkFile("restore") { path ->
        writeWorkFile(path, bytes)
        openBackupConnection(databaseName, passphrase).use { connection ->
            // SQLCipher checks the key while attaching: a wrong one fails with "file is not a database".
            try {
                connection.execute("ATTACH DATABASE ? AS $BACKUP_SCHEMA KEY ?", path, password)
            } catch (@Suppress("TooGenericExceptionCaught", "SwallowedException") error: Exception) {
                throw PhysioBackupException.InvalidFileOrPassword()
            }
            try {
                val info = connection.readBackupInfo()
                if (requireNoAccounts && connection.query("SELECT count(*) FROM main.account").single()[0] != "0") {
                    throw PhysioBackupException.DeviceHasAccounts()
                }
                info to connection.replaceAllRows()
            } finally {
                runCatching { connection.execute("DETACH DATABASE $BACKUP_SCHEMA") }
            }
        }
    }
    // The rows changed behind SQLDelight's back: refresh every query that is being observed.
    tables.forEach { driver.notifyListeners(it) }
    return info
}

private fun BackupConnection.readBackupInfo(): PhysioBackupInfo {
    // A missing or unreadable info table means this is not a PhysioApp backup.
    val row = runCatching {
        query("SELECT format, app, schema_version, created_at FROM $BACKUP_SCHEMA.$BACKUP_INFO_TABLE").singleOrNull()
    }.getOrNull()
    val schemaVersion = row
        ?.takeIf { it[0] == BACKUP_FORMAT.toString() && it[1] == BACKUP_APP_ID }
        ?.get(2)
        ?.toLongOrNull()
    val createdAt = row?.get(3)?.toLongOrNull()
    if (schemaVersion == null || createdAt == null) throw PhysioBackupException.InvalidFileOrPassword()
    val current = PhysioDatabase.Schema.version
    if (schemaVersion != current) {
        throw PhysioBackupException.IncompatibleVersion(madeWithNewerApp = schemaVersion > current)
    }
    return PhysioBackupInfo(createdAt)
}

/** Copies every table of the attached backup over the main database; returns the table names. */
private fun BackupConnection.replaceAllRows(): List<String> {
    val tables = tableNames("main")
    if (tableNames(BACKUP_SCHEMA).toSet() != tables.toSet() + BACKUP_INFO_TABLE) {
        throw PhysioBackupException.InvalidFileOrPassword()
    }
    // Rows go in table by table, so references are only checked once all of them are in.
    execute("PRAGMA foreign_keys = OFF")
    try {
        transaction {
            tables.forEach { execute("DELETE FROM main.\"$it\"") }
            tables.forEach { table ->
                val columns = query("PRAGMA main.table_info(\"$table\")").joinToString { "\"${it[1]}\"" }
                execute("INSERT INTO main.\"$table\" ($columns) SELECT $columns FROM $BACKUP_SCHEMA.\"$table\"")
            }
            if (query("PRAGMA main.foreign_key_check").isNotEmpty()) {
                throw PhysioBackupException.InvalidFileOrPassword()
            }
        }
    } finally {
        execute("PRAGMA foreign_keys = ON")
    }
    return tables
}

private fun BackupConnection.tableNames(schema: String): List<String> =
    query("SELECT name FROM $schema.sqlite_master WHERE type = 'table' AND name NOT LIKE 'sqlite_%'")
        .mapNotNull { it[0] }

/** Runs [block] with a path inside a fresh private work folder, which is deleted afterwards. */
private inline fun <T> withBackupWorkFile(prefix: String, block: (String) -> T): T {
    val folder = createBackupWorkFolder()
    try {
        return block("$folder/$prefix.physiobackup")
    } finally {
        deleteBackupWorkFolder(folder)
    }
}

/**
 * A connection of its own to the database file, used only by backups: ATTACH must run outside a
 * transaction on one specific connection, which SQLDelight's drivers do not expose.
 */
internal interface BackupConnection : AutoCloseable {
    /** Runs a statement that returns no rows; [args] are bound as text, in order. */
    fun execute(sql: String, vararg args: String)

    /** Every row of [sql], each column as text (null for SQL NULL). */
    fun query(sql: String): List<List<String?>>

    /** Runs [block] in one transaction, rolled back if it throws. */
    fun transaction(block: () -> Unit)
}

internal expect fun openBackupConnection(databaseName: String, passphrase: String): BackupConnection

/** Creates a new, empty folder in the app's private temporary storage and returns its path. */
internal expect fun createBackupWorkFolder(): String

internal expect fun deleteBackupWorkFolder(path: String)

internal expect fun writeWorkFile(path: String, bytes: ByteArray)

internal expect fun readWorkFile(path: String): ByteArray
