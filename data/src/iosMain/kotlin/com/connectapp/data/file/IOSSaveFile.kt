package com.connectapp.data.file

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.writeToURL
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
class IOSSaveFile : SaveFile {

    override fun saveAndExport(fileName: String, bytes: ByteArray): Boolean {
        return runCatching {
            val nameWithExtension = if (fileName.endsWith(".pdf", ignoreCase = true)) {
                fileName
            } else {
                "$fileName.pdf"
            }

            val fileManager = NSFileManager.defaultManager
            val documentsUrl = fileManager.URLsForDirectory(NSDocumentDirectory, NSUserDomainMask)
                .first() as? NSURL
                ?: return false

            val fileUrl =
                documentsUrl.URLByAppendingPathComponent(nameWithExtension) ?: return false

            val data = bytes.usePinned { pinned ->
                NSData.create(
                    bytes = pinned.addressOf(0),
                    length = bytes.size.toULong()
                )
            }

            data.writeToURL(fileUrl, atomically = true)
        }.getOrDefault(false)
    }

    override fun notifyOrShareFile(fileName: String) {
        val nameWithExtension = if (fileName.endsWith(".pdf", ignoreCase = true)) {
            fileName
        } else {
            "$fileName.pdf"
        }

        val fileManager = NSFileManager.defaultManager
        val documentsUrl = fileManager.URLsForDirectory(NSDocumentDirectory, NSUserDomainMask)
            .first() as? NSURL ?: return

        val fileUrl = documentsUrl.URLByAppendingPathComponent(nameWithExtension) ?: return

        // 2. Crear el controlador nativo para compartir
        val activityViewController = UIActivityViewController(
            activityItems = listOf(fileUrl),
            applicationActivities = null
        )

        // 3. Buscar la ventana principal para poder mostrar el menú emergente
        val windowScene =
            UIApplication.sharedApplication.connectedScenes.firstOrNull() as? UIWindowScene
        val window = windowScene?.windows?.firstOrNull() as? UIWindow
        val rootViewController = window?.rootViewController

        // 4. Presentarlo en pantalla
        rootViewController?.presentViewController(
            activityViewController,
            animated = true,
            completion = null
        )
    }
}
