@file:OptIn(ExperimentalForeignApi::class)

package com.connectapp.commonresources.export

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfURL
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UniformTypeIdentifiers.UTTypeData
import platform.darwin.NSObject
import platform.posix.memcpy

/**
 * iOS: the Files picker in "copy" mode, so the app gets its own temporary copy of the file, which
 * is deleted as soon as it has been read.
 */
@Composable
actual fun rememberDocumentPicker(onPicked: (PickedDocument?) -> Unit): DocumentPicker {
    val currentOnPicked by rememberUpdatedState(onPicked)
    return remember { IosDocumentPicker { currentOnPicked(it) } }
}

private class IosDocumentPicker(private val onPicked: (PickedDocument?) -> Unit) : DocumentPicker {
    // UIKit keeps the picker's delegate weakly: hold it until the picker closes.
    private var delegate: NSObject? = null

    override fun pick() {
        val presenter = topViewController() ?: return onPicked(null)
        val picker = UIDocumentPickerViewController(forOpeningContentTypes = listOf(UTTypeData), asCopy = true)
        val pickerDelegate = OpenPickerDelegate { url ->
            delegate = null
            onPicked(url?.let(::readAndDelete))
        }
        delegate = pickerDelegate
        picker.delegate = pickerDelegate
        picker.allowsMultipleSelection = false
        presenter.presentViewController(picker, animated = true, completion = null)
    }
}

private class OpenPickerDelegate(private val onFinished: (NSURL?) -> Unit) :
    NSObject(), UIDocumentPickerDelegateProtocol {
    override fun documentPicker(controller: UIDocumentPickerViewController, didPickDocumentsAtURLs: List<*>) =
        onFinished(didPickDocumentsAtURLs.firstOrNull() as? NSURL)

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) = onFinished(null)
}

private fun readAndDelete(url: NSURL): PickedDocument? {
    val data = NSData.dataWithContentsOfURL(url)
    url.path?.let { NSFileManager.defaultManager.removeItemAtPath(it, error = null) }
    return data?.let { PickedDocument(url.lastPathComponent.orEmpty(), it.toByteArray()) }
}

private fun NSData.toByteArray(): ByteArray {
    val bytes = ByteArray(length.toInt())
    if (bytes.isNotEmpty()) bytes.usePinned { memcpy(it.addressOf(0), this.bytes, length) }
    return bytes
}
