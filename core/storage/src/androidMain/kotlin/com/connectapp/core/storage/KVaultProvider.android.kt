package com.connectapp.core.storage

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import com.liftric.kvault.KVault

/**
 * Holds the Android application [Context] needed to construct [KVault].
 *
 * Rather than requiring the app module to wire a Koin `androidContext()` call itself, the context
 * is captured automatically at process start via [KVaultContextInitializer], a manifest-declared
 * ContentProvider — the same "auto-init" pattern AndroidX Startup/WorkManager use to obtain a
 * Context without any app-side code. Android guarantees content providers are created before any
 * Activity, so [AndroidContextHolder.applicationContext] is available by the time [createKVault]
 * is called.
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
class KVaultContextInitializer : ContentProvider() {

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

actual fun createKVault(name: String): KVault = KVault(
    context = AndroidContextHolder.applicationContext,
    fileName = name
)

actual fun createDeviceBoundKVault(name: String): KVault = createKVault(name)
