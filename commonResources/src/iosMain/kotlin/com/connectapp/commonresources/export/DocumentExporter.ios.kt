@file:OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)

package com.connectapp.commonresources.export

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSData
import platform.Foundation.NSDataWritingAtomic
import platform.Foundation.NSDataWritingFileProtectionComplete
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.create
import platform.Foundation.writeToFile
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.UIKit.popoverPresentationController
import platform.darwin.NSObject

/**
 * iOS: the file is written to the app's temporary folder with complete file protection, handed
 * to the share sheet or the "save to Files" picker, and deleted as soon as either closes (D19).
 */
@Composable
actual fun rememberDocumentExporter(): DocumentExporter = remember { IosDocumentExporter() }

@Composable
actual fun PurgeExportedDocumentsEffect() {
    LaunchedEffect(Unit) { removeExports() }
}

private class IosDocumentExporter : DocumentExporter {
    // UIKit keeps the picker's delegate weakly: hold it until the picker closes.
    private var pickerDelegate: NSObject? = null

    override fun share(document: ExportedDocument) {
        val url = writeExportFile(document) ?: return
        val presenter = topViewController() ?: return removeExports()
        val controller = UIActivityViewController(activityItems = listOf(url), applicationActivities = null)
        controller.completionWithItemsHandler = { _, _, _, _ -> removeExports() }
        // On iPad the share sheet is a popover and must be anchored, or UIKit throws.
        controller.popoverPresentationController?.let { popover ->
            val view = presenter.view
            popover.sourceView = view
            popover.sourceRect = view.bounds.useContents { CGRectMake(size.width / 2, size.height / 2, 0.0, 0.0) }
            popover.permittedArrowDirections = 0u
        }
        presenter.presentViewController(controller, animated = true, completion = null)
    }

    override fun save(document: ExportedDocument, onResult: (Boolean) -> Unit) {
        val url = writeExportFile(document) ?: return onResult(false)
        val presenter = topViewController() ?: return run {
            removeExports()
            onResult(false)
        }
        val picker = UIDocumentPickerViewController(forExportingURLs = listOf(url), asCopy = true)
        val delegate = PickerDelegate { saved ->
            pickerDelegate = null
            removeExports()
            onResult(saved)
        }
        pickerDelegate = delegate
        picker.delegate = delegate
        presenter.presentViewController(picker, animated = true, completion = null)
    }
}

private class PickerDelegate(private val onFinished: (picked: Boolean) -> Unit) :
    NSObject(), UIDocumentPickerDelegateProtocol {
    override fun documentPicker(controller: UIDocumentPickerViewController, didPickDocumentsAtURLs: List<*>) =
        onFinished(true)

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) = onFinished(false)
}

private fun exportDirectory(): String = NSTemporaryDirectory().trimEnd('/') + "/" + EXPORT_FOLDER

private fun writeExportFile(document: ExportedDocument): NSURL? {
    removeExports()
    NSFileManager.defaultManager.createDirectoryAtPath(
        exportDirectory(),
        withIntermediateDirectories = true,
        attributes = null,
        error = null,
    )
    val path = exportDirectory() + "/" + document.fileName
    val written = document.bytes.toNSData().writeToFile(
        path,
        options = NSDataWritingFileProtectionComplete or NSDataWritingAtomic,
        error = null,
    )
    return if (written) NSURL.fileURLWithPath(path) else null
}

private fun removeExports() {
    NSFileManager.defaultManager.removeItemAtPath(exportDirectory(), error = null)
}

internal fun ByteArray.toNSData(): NSData = usePinned { pinned ->
    NSData.create(bytes = pinned.addressOf(0), length = size.convert())
}

/** The view controller on top of the key window, to present system sheets from. */
internal fun topViewController(): UIViewController? {
    val window = UIApplication.sharedApplication.connectedScenes
        .filterIsInstance<UIWindowScene>()
        .flatMap { scene -> scene.windows.filterIsInstance<UIWindow>() }
        .firstOrNull { it.isKeyWindow() }
    var controller = window?.rootViewController
    while (controller?.presentedViewController != null) controller = controller.presentedViewController
    return controller
}
