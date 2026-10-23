package com.connectapp.core.database

import androidx.sqlite.db.SupportSQLiteDatabase
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

// sqlcipher-android ships its own native library, which must be loaded before first use.
internal val sqlCipherLoaded: Unit by lazy { System.loadLibrary("sqlcipher") }

actual fun createEncryptedSqlDriver(
    schema: SqlSchema<QueryResult.Value<Unit>>,
    databaseName: String,
    passphrase: String,
): SqlDriver {
    sqlCipherLoaded
    return AndroidSqliteDriver(
        schema = schema,
        context = AndroidContextHolder.applicationContext,
        name = databaseName,
        factory = SupportOpenHelperFactory(passphrase.encodeToByteArray()),
        callback = object : AndroidSqliteDriver.Callback(schema) {
            override fun onConfigure(db: SupportSQLiteDatabase) {
                super.onConfigure(db)
                db.setForeignKeyConstraintsEnabled(true)
            }
        },
    )
}

// Backups are already off for the whole app (AndroidManifest: allowBackup="false" + data
// extraction rules), and the app sandbox is protected by Android's file-based encryption.
internal actual fun protectDatabaseFiles(databaseName: String) = Unit
