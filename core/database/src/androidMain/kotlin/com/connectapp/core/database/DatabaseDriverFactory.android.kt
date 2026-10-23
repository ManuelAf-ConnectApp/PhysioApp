package com.connectapp.core.database

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

/**
 * Holds the Android application [Context] needed to construct [AndroidSqliteDriver].
 *
 * Same "auto-init" pattern as `core:storage`'s `AndroidContextHolder`/`KVaultContextInitializer`
 * (see KVaultProvider.android.kt) — duplicated here rather than shared, since that holder is
 * `internal` to `core:storage` and this module intentionally has no dependency on it (research.md
 * D3 in specs/010-offline-cache-layer: two independent `core:*` modules, not a new `core -> core`
 * coupling for a startup detail).
 */
internal object AndroidContextHolder {
    lateinit var applicationContext: Context
        private set

    internal fun install(context: Context) {
        if (!::applicationContext.isInitialized) {
            applicationContext = context.applicationContext
        }
    }
}

/** Registered in this module's AndroidManifest.xml; merged into the host app's manifest automatically. */
class DatabaseContextInitializer : ContentProvider() {

    override fun onCreate(): Boolean {
        context?.let { AndroidContextHolder.install(it) }
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? = null

    override fun getType(uri: Uri): String? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int = 0
}

actual fun createSqlDriver(schema: SqlSchema<QueryResult.Value<Unit>>, databaseName: String): SqlDriver =
    AndroidSqliteDriver(
        schema = schema,
        context = AndroidContextHolder.applicationContext,
        name = databaseName,
    )
