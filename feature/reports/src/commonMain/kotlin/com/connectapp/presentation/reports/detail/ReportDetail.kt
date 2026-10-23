package com.connectapp.presentation.reports.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.connectapp.commonresources.annul_report_message
import com.connectapp.commonresources.annul_report_title
import com.connectapp.commonresources.button_annul
import com.connectapp.commonresources.button_cancel
import com.connectapp.commonresources.button_edit
import com.connectapp.commonresources.button_export_pdf
import com.connectapp.commonresources.button_hide_versions
import com.connectapp.commonresources.button_show_versions
import com.connectapp.commonresources.component.EmptyState
import com.connectapp.commonresources.component.ErrorMessage
import com.connectapp.commonresources.component.LoadingIndicator
import com.connectapp.commonresources.component.RecordField
import com.connectapp.commonresources.export.DocumentExportDialog
import com.connectapp.commonresources.format.formatDateTime
import com.connectapp.commonresources.label_annulment_reason
import com.connectapp.commonresources.label_condition_text
import com.connectapp.commonresources.label_patient
import com.connectapp.commonresources.label_professional
import com.connectapp.commonresources.label_report_text
import com.connectapp.commonresources.report_annulled_on
import com.connectapp.commonresources.report_created_on
import com.connectapp.commonresources.report_modified_on
import com.connectapp.commonresources.report_not_author_notice
import com.connectapp.commonresources.report_not_found
import com.connectapp.commonresources.report_version_range
import com.connectapp.domain.reports.ClinicalReport
import com.connectapp.domain.reports.ReportVersion
import com.connectapp.presentation.reports.pdf.exportReportPdf
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * A report as the detail pane of the search or as a screen of its own. Edit and annul are only
 * offered to its author while it is active (D17).
 */
@Composable
fun ReportDetail(
    reportId: String,
    onEdit: (reportId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: ReportDetailViewModel = koinViewModel(key = "report-detail-$reportId") { parametersOf(reportId) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val report = state.report

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        when {
            state.isLoading -> LoadingIndicator(modifier = Modifier.align(Alignment.Center))
            report == null -> EmptyState(Icons.Outlined.Description, stringResource(report_not_found), "")
            else -> DetailContent(report, state, viewModel::onIntent, onEdit = { onEdit(report.id) })
        }
    }

    state.annulmentReason?.let { reason ->
        AnnulmentDialog(
            reason = reason,
            canConfirm = state.canConfirmAnnulment,
            onReasonChange = { viewModel.onIntent(ReportDetailIntent.AnnulmentReasonChanged(it)) },
            onConfirm = { viewModel.onIntent(ReportDetailIntent.AnnulmentConfirmed) },
            onDismiss = { viewModel.onIntent(ReportDetailIntent.AnnulmentDismissed) },
        )
    }

    if (state.isExporting && report != null) {
        DocumentExportDialog(
            build = { exportReportPdf(report) },
            onDismiss = { viewModel.onIntent(ReportDetailIntent.ExportDismissed) },
        )
    }
}

@Composable
private fun DetailContent(
    report: ClinicalReport,
    state: ReportDetailState,
    onIntent: (ReportDetailIntent) -> Unit,
    onEdit: () -> Unit,
) {
    Column(
        modifier = Modifier
            .widthIn(max = 720.dp)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(report.code, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.semantics { heading() })
        Text(
            text = stringResource(report_created_on, formatDateTime(report.createdAtEpochMillis)),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        report.annulment?.let { annulment ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = stringResource(
                        report_annulled_on,
                        formatDateTime(annulment.annulledAtEpochMillis),
                        annulment.reason,
                    ),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(16.dp),
                )
            }
        }

        val patient = report.patient
        RecordField(label_patient, "${patient.firstName} ${patient.lastName} · ${patient.documentNumber}")
        RecordField(label_professional, "${report.professional.firstName} ${report.professional.lastName}")
        RecordField(label_report_text, report.reportText)
        RecordField(label_condition_text, report.conditionText)

        if (report.wasEdited) {
            Text(
                text = stringResource(
                    report_modified_on,
                    formatDateTime(report.updatedAtEpochMillis),
                    report.versionCount,
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            TextButton(onClick = { onIntent(ReportDetailIntent.VersionsToggled) }) {
                Text(stringResource(if (state.showVersions) button_hide_versions else button_show_versions))
            }
            if (state.showVersions) state.versions.forEach { VersionCard(it) }
        }

        state.errorMessage?.let {
            ErrorMessage(stringResource(it))
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (state.canChange) Button(onClick = onEdit) { Text(stringResource(button_edit)) }
            OutlinedButton(onClick = { onIntent(ReportDetailIntent.ExportClicked) }) {
                Text(stringResource(button_export_pdf))
            }
            if (state.canChange) {
                OutlinedButton(
                    onClick = { onIntent(ReportDetailIntent.AnnulClicked) },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) {
                    Text(stringResource(button_annul))
                }
            }
        }
        if (!state.isAuthor && !report.isAnnulled) {
            Text(
                text = stringResource(report_not_author_notice),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun VersionCard(version: ReportVersion) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(
                    report_version_range,
                    formatDateTime(version.savedAtEpochMillis),
                    formatDateTime(version.replacedAtEpochMillis),
                ),
                style = MaterialTheme.typography.labelLarge,
            )
            RecordField(label_report_text, version.reportText)
            RecordField(label_condition_text, version.conditionText)
        }
    }
}

@Composable
private fun AnnulmentDialog(
    reason: String,
    canConfirm: Boolean,
    onReasonChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(annul_report_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(annul_report_message))
                OutlinedTextField(
                    value = reason,
                    onValueChange = onReasonChange,
                    label = { Text(stringResource(label_annulment_reason)) },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = canConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
            ) {
                Text(stringResource(button_annul))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(button_cancel)) } },
    )
}

/** The report as a screen of its own (windows too narrow for list and detail side by side). */
@Composable
fun ReportDetailScreen(reportId: String, onEdit: (reportId: String) -> Unit, modifier: Modifier = Modifier) {
    ReportDetail(reportId = reportId, onEdit = onEdit, modifier = modifier)
}
