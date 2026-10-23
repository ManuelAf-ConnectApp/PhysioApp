package com.connectapp.commonresources.export

import androidx.compose.runtime.Composable

/** A file the user picked from outside the app, read whole into memory. */
class PickedDocument(val fileName: String, val bytes: ByteArray) {
    override fun toString(): String = "PickedDocument(fileName=$fileName, size=${bytes.size})"
}

/**
 * Opens the system file picker (Android "open document", iPad Files) to bring a file into the
 * app, such as a backup to restore. The app reads a private copy, deleted once it has been read.
 */
fun interface DocumentPicker {
    fun pick()
}

/** [onPicked] gets the file, or null when the user cancels or it cannot be read. */
@Composable
expect fun rememberDocumentPicker(onPicked: (PickedDocument?) -> Unit): DocumentPicker
