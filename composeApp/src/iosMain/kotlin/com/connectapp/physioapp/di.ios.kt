package com.connectapp.physioapp

import com.connectapp.data.database.DatabaseDriverFactory
import com.connectapp.data.database.IOSDatabaseTransfer
import com.connectapp.data.database.IosDatabaseDriverFactory
import com.connectapp.data.file.SaveFile
import com.connectapp.data.file.IOSSaveFile
import com.connectapp.data.notification.IOSNotificationPermissionManager
import com.connectapp.data.storage.CryptoManager
import com.connectapp.data.storage.IosCryptoManager
import com.connectapp.domain.database.DatabaseTransfer
import com.connectapp.domain.repository.NotificationPermissionRepository
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
actual val platformModule: Module = module {
    single<DatabaseDriverFactory> {
        IosDatabaseDriverFactory()
    }
    single<CryptoManager> { IosCryptoManager() }

    single(named("datastore_path")) {
        val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null
        )
        (documentDirectory?.path + "/secrets.preferences_pb")
    }

    single<SaveFile> { IOSSaveFile() }

    single<DatabaseTransfer> { IOSDatabaseTransfer() }

    single<NotificationPermissionRepository> { IOSNotificationPermissionManager() }
}
