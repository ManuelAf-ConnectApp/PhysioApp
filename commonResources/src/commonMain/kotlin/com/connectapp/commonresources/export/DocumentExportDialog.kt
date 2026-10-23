package com.connectapp.commonresources.export

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import com.connectapp.commonresources.button_cancel
import com.connectapp.commonresources.button_save_file
import com.connectapp.commonresources.button_share
import com.connectapp.commonresources.component.LoadingIndicator
import com.connectapp.commonresources.export_pdf_error
import com.connectapp.commonresources.export_pdf_title
import com.connectapp.commonresources.export_pdf_warning
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

/**
 * "Exportar PDF" (D17, D19): warns that the document leaves the app's
 * protection, then builds it with [build] and shares or saves it.
 */
@Composable
fun DocumentExportDialog(build: suspend () -> ExportedDocument, onDismiss: () -> Unit) {
    val exporter = rememberDocumentExporter()
    val scope = rememberCoroutineScope()
    var isBuilding by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }

    fun export(deliver: (ExportedDocument) -> Unit) {
        isBuilding = true
        failed = false
        scope.launch {
            try {
                deliver(build())
                onDismiss()
            } catch (error: CancellationException) {
                throw error
            } catch (@Suppress("TooGenericExceptionCaught", "SwallowedException") error: Exception) {
                // The dialog says the PDF could not be generated; its content never reaches logs.
                failed = true
            } finally {
                isBuilding = false
            }
        }
    }

    AlertDialog(
        onDismissRequest = { if (!isBuilding) onDismiss() },
        title = { Text(stringResource(export_pdf_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(export_pdf_warning))
                if (isBuilding) LoadingIndicator()
                if (failed) Text(stringResource(export_pdf_error), color = MaterialTheme.colorScheme.error)
            }
        },
        confirmButton = {
            Row {
                TextButton(onClick = { export(exporter::save) }, enabled = !isBuilding) {
                    Text(stringResource(button_save_file))
                }
                TextButton(onClick = { export(exporter::share) }, enabled = !isBuilding) {
                    Text(stringResource(button_share))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isBuilding) { Text(stringResource(button_cancel)) }
        },
    )
}
