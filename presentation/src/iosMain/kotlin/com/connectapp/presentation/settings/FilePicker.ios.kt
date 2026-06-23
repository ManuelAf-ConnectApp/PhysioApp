package com.connectapp.presentation.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.UIKit.*
import platform.Foundation.*
import platform.UniformTypeIdentifiers.*
import platform.darwin.NSObject

@Composable
actual fun rememberFilePickerLauncher(onResult: (String?) -> Unit): () -> Unit {
    val delegate = remember {
        object : NSObject(), UIDocumentPickerDelegateProtocol {
            override fun documentPicker(controller: UIDocumentPickerViewController, didPickDocumentsAtURLs: List<*>) {
                val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL
                onResult(url?.path)
            }

            override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
                onResult(null)
            }
        }
    }

    return {
        val picker = UIDocumentPickerViewController(
            forOpeningContentTypes = listOf(UTTypeData),
            asCopy = true,
        )
        picker.delegate = delegate
        
        val rootViewController = UIApplication.sharedApplication.keyWindow?.rootViewController
        rootViewController?.presentViewController(picker, animated = true, completion = null)
    }
}
