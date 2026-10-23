package com.connectapp.core.database

import net.zetetic.database.sqlcipher.SQLiteDatabase
import java.io.File
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/** Subfolder of the app's private cache directory where backups are written and read. */
private const val BACKUP_WORK_FOLDER = "backup"

internal actual fun openBackupConnection(databaseName: String, passphrase: String): BackupConnection {
    sqlCipherLoaded
    val path = AndroidContextHolder.applicationContext.getDatabasePath(databaseName).absolutePath
    val database = SQLiteDatabase.openDatabase(
        path,
        passphrase.encodeToByteArray(),
        null,
        SQLiteDatabase.OPEN_READWRITE,
        null,
        null,
    )
    return AndroidBackupConnection(database)
}

private class AndroidBackupConnection(private val database: SQLiteDatabase) : BackupConnection {
    override fun execute(sql: String, vararg args: String) {
        if (args.isEmpty()) database.execSQL(sql) else database.execSQL(sql, arrayOf<Any>(*args))
    }

    override fun query(sql: String): List<List<String?>> = database.rawQuery(sql, null).use { cursor ->
        buildList {
            while (cursor.moveToNext()) {
                add((0 until cursor.columnCount).map { if (cursor.isNull(it)) null else cursor.getString(it) })
            }
        }
    }

    override fun transaction(block: () -> Unit) {
        database.beginTransaction()
        try {
            block()
            database.setTransactionSuccessful()
        } finally {
            database.endTransaction()
        }
    }

    override fun close() = database.close()
}

@OptIn(ExperimentalUuidApi::class)
internal actual fun createBackupWorkFolder(): String {
    val root = File(AndroidContextHolder.applicationContext.cacheDir, BACKUP_WORK_FOLDER)
    // Leftovers of an operation interrupted by the app being killed.
    root.deleteRecursively()
    return File(root, Uuid.random().toString()).apply { mkdirs() }.absolutePath
}

internal actual fun deleteBackupWorkFolder(path: String) {
    File(path).deleteRecursively()
}

internal actual fun writeWorkFile(path: String, bytes: ByteArray) = File(path).writeBytes(bytes)

internal actual fun readWorkFile(path: String): ByteArray = File(path).readBytes()
