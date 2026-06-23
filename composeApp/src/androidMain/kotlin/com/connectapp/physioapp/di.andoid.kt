package com.connectapp.physioapp

import android.content.Context
import com.connectapp.data.database.AndroidDatabaseDriverFactory
import com.connectapp.data.database.AndroidDatabaseTransfer
import com.connectapp.data.database.DatabaseDriverFactory
import com.connectapp.data.file.AndroidSaveFile
import com.connectapp.data.file.SaveFile
import com.connectapp.data.notification.AndroidNotificationPermissionManager
import com.connectapp.data.storage.AndroidCryptoManager
import com.connectapp.data.storage.CryptoManager
import com.connectapp.domain.database.DatabaseTransfer
import com.connectapp.domain.repository.NotificationPermissionRepository
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module


actual val platformModule: Module = module {
    single<DatabaseDriverFactory> {
        AndroidDatabaseDriverFactory(context = get())
    }
    single<CryptoManager> { AndroidCryptoManager() }
    single(named("datastore_path")) {
        val context: Context = get()
        context.filesDir.resolve("secrets.preferences_pb").absolutePath
    }

    single<SaveFile> { AndroidSaveFile(context = get()) }

    single<DatabaseTransfer> { AndroidDatabaseTransfer(context = get()) }

    factory<NotificationPermissionRepository> { AndroidNotificationPermissionManager(activity = get()) }
}
