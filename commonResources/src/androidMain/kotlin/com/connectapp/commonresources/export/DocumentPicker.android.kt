package com.connectapp.commonresources.export

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Android: the system "open document" picker; the bytes are read through the returned URI. */
@Composable
actual fun rememberDocumentPicker(onPicked: (PickedDocument?) -> Unit): DocumentPicker {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentOnPicked by rememberUpdatedState(onPicked)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) {
            currentOnPicked(null)
        } else {
            scope.launch {
                currentOnPicked(withContext(Dispatchers.IO) { runCatching { readDocument(context, uri) }.getOrNull() })
            }
        }
    }
    // Backups have no registered MIME type, so every file can be chosen; the app checks the content.
    return remember(launcher) { DocumentPicker { launcher.launch(arrayOf("*/*")) } }
}

private fun readDocument(context: Context, uri: Uri): PickedDocument {
    val resolver = context.contentResolver
    val name = resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) cursor.getString(0) else null
    }
    val bytes = checkNotNull(resolver.openInputStream(uri)).use { it.readBytes() }
    return PickedDocument(name ?: uri.lastPathSegment.orEmpty(), bytes)
}
