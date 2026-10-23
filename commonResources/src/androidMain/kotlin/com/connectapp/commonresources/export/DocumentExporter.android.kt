package com.connectapp.commonresources.export

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/** Authority of the FileProvider declared in composeApp's AndroidManifest.xml. */
private const val AUTHORITY_SUFFIX = ".exports"

/**
 * Android: sharing goes through a FileProvider URI with a read grant for the chosen app; saving
 * uses the system "create document" picker and writes the bytes straight to the chosen place.
 *
 * Android does not say when the receiving app has finished reading a shared file, so the copy in
 * the private cache is deleted before the next export and when the app starts (D19), never right
 * after the share sheet closes.
 */
@Composable
actual fun rememberDocumentExporter(): DocumentExporter {
    val context = LocalContext.current
    val pendingSave = remember { mutableStateOf<Pair<ExportedDocument, (Boolean) -> Unit>?>(null) }
    val saveLauncher = rememberLauncherForActivityResult(CreateTypedDocument()) { uri ->
        val (document, onResult) = pendingSave.value ?: return@rememberLauncherForActivityResult
        pendingSave.value = null
        val saved = uri != null && runCatching {
            checkNotNull(context.contentResolver.openOutputStream(uri)).use { it.write(document.bytes) }
        }.isSuccess
        onResult(saved)
    }
    return remember(context, saveLauncher) {
        object : DocumentExporter {
            override fun share(document: ExportedDocument) {
                val file = writeExportFile(context, document)
                val uri = FileProvider.getUriForFile(context, context.packageName + AUTHORITY_SUFFIX, file)
                val send = Intent(Intent.ACTION_SEND).apply {
                    type = document.mimeType
                    putExtra(Intent.EXTRA_STREAM, uri)
                    clipData = ClipData.newRawUri(document.fileName, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(send, null))
            }

            override fun save(document: ExportedDocument, onResult: (Boolean) -> Unit) {
                purgeExports(context)
                pendingSave.value = document to onResult
                saveLauncher.launch(document)
            }
        }
    }
}

/** The system "create document" picker with the MIME type of each document (CreateDocument fixes one). */
private class CreateTypedDocument : ActivityResultContract<ExportedDocument, Uri?>() {
    override fun createIntent(context: Context, input: ExportedDocument): Intent =
        Intent(Intent.ACTION_CREATE_DOCUMENT)
            .addCategory(Intent.CATEGORY_OPENABLE)
            .setType(input.mimeType)
            .putExtra(Intent.EXTRA_TITLE, input.fileName)

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? = intent?.data
}

@Composable
actual fun PurgeExportedDocumentsEffect() {
    val context = LocalContext.current
    LaunchedEffect(Unit) { withContext(Dispatchers.IO) { purgeExports(context) } }
}

private fun exportFolder(context: Context) = File(context.cacheDir, EXPORT_FOLDER)

private fun writeExportFile(context: Context, document: ExportedDocument): File {
    purgeExports(context)
    val folder = exportFolder(context).apply { mkdirs() }
    return File(folder, document.fileName).apply { writeBytes(document.bytes) }
}

private fun purgeExports(context: Context) {
    exportFolder(context).deleteRecursively()
}
