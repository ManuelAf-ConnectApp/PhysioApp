package com.connectapp.commonresources.export

import androidx.compose.runtime.Composable

/** MIME type of the PDFs the app exports. */
const val PDF_MIME_TYPE = "application/pdf"

/** MIME type for files with no registered type, such as backups. */
const val BINARY_MIME_TYPE = "application/octet-stream"

/** A generated file ready to leave the app; [fileName] never carries personal data. */
class ExportedDocument(val fileName: String, val bytes: ByteArray, val mimeType: String = PDF_MIME_TYPE) {
    override fun toString(): String = "ExportedDocument(fileName=$fileName, size=${bytes.size}, mimeType=$mimeType)"
}

/**
 * Hands a document to the system (D19, 2026-10-20): [share] opens the share sheet and [save] the
 * "save to" picker. The file only exists in the app's private temporary folder while it is being
 * shared, and is never protected by the app once it has left.
 */
interface DocumentExporter {
    fun share(document: ExportedDocument)

    /** [onResult] gets true once the file has been written to the chosen place, false if cancelled or failed. */
    fun save(document: ExportedDocument, onResult: (saved: Boolean) -> Unit = {})
}

@Composable
expect fun rememberDocumentExporter(): DocumentExporter

/**
 * Deletes what earlier exports may have left in the temporary folder (an app closed in the
 * middle of sharing). Called once from the app shell.
 */
@Composable
expect fun PurgeExportedDocumentsEffect()

/** Subfolder of the platform's private cache/temporary directory that holds exported files. */
internal const val EXPORT_FOLDER = "exports"
