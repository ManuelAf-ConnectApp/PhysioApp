package com.connectapp.presentation.patients.detail

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PersonOff
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.connectapp.commonresources.archive_patient_message
import com.connectapp.commonresources.archive_patient_title
import com.connectapp.commonresources.badge_archived
import com.connectapp.commonresources.button_archive
import com.connectapp.commonresources.button_delete_permanently
import com.connectapp.commonresources.button_edit
import com.connectapp.commonresources.button_sign_again
import com.connectapp.commonresources.button_unarchive
import com.connectapp.commonresources.component.ConfirmationDialog
import com.connectapp.commonresources.component.EmptyState
import com.connectapp.commonresources.component.ErrorMessage
import com.connectapp.commonresources.component.LoadingIndicator
import com.connectapp.commonresources.component.RecordField
import com.connectapp.commonresources.delete_patient_message
import com.connectapp.commonresources.delete_patient_title
import com.connectapp.commonresources.format.formatDateTime
import com.connectapp.commonresources.label_address
import com.connectapp.commonresources.label_document_number
import com.connectapp.commonresources.label_document_type
import com.connectapp.commonresources.label_email
import com.connectapp.commonresources.label_notes
import com.connectapp.commonresources.label_phone
import com.connectapp.commonresources.no_signature
import com.connectapp.commonresources.patient_not_found
import com.connectapp.commonresources.professional_has_records_notice
import com.connectapp.commonresources.signature.SIGNATURE_ASPECT_RATIO
import com.connectapp.commonresources.signature_history_count
import com.connectapp.commonresources.signature_image_description
import com.connectapp.commonresources.signature_section_title
import com.connectapp.commonresources.signature_signed_by_patient
import com.connectapp.commonresources.signature_signed_by_representative
import com.connectapp.commonresources.signature_signed_on
import com.connectapp.domain.patients.Patient
import com.connectapp.domain.patients.SignatureInfo
import com.connectapp.domain.patients.SignatureSigner
import com.connectapp.presentation.patients.label
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.decodeToImageBitmap
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Record of one patient with its signed consent and its actions. Used as the detail pane of the
 * search and as a screen of its own when list and detail do not fit side by side.
 */
@Composable
fun PatientDetail(
    patientId: String,
    onEdit: (patientId: String) -> Unit,
    onSignAgain: (patientId: String) -> Unit,
    onDeleted: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: PatientDetailViewModel = koinViewModel(key = "patient-detail-$patientId") { parametersOf(patientId) }
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                PatientDetailEffect.Deleted -> onDeleted()
            }
        }
    }

    val patient = state.patient
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        when {
            state.isLoading -> LoadingIndicator(modifier = Modifier.align(Alignment.Center))
            patient == null -> EmptyState(Icons.Outlined.PersonOff, stringResource(patient_not_found), "")
            else -> DetailContent(
                patient = patient,
                signaturePng = state.signaturePng,
                errorMessage = state.errorMessage,
                onIntent = viewModel::onIntent,
                onEdit = { onEdit(patient.id) },
                onSignAgain = { onSignAgain(patient.id) },
            )
        }
    }

    state.dialog?.let { dialog ->
        val archive = dialog == PatientDialog.ARCHIVE
        ConfirmationDialog(
            title = if (archive) archive_patient_title else delete_patient_title,
            message = if (archive) archive_patient_message else delete_patient_message,
            confirm = if (archive) button_archive else button_delete_permanently,
            onConfirm = { viewModel.onIntent(PatientDetailIntent.DialogConfirmed) },
            onDismiss = { viewModel.onIntent(PatientDetailIntent.DialogDismissed) },
        )
    }
}

@Composable
private fun DetailContent(
    patient: Patient,
    signaturePng: ByteArray?,
    errorMessage: StringResource?,
    onIntent: (PatientDetailIntent) -> Unit,
    onEdit: () -> Unit,
    onSignAgain: () -> Unit,
) {
    Column(
        modifier = Modifier
            .widthIn(max = 720.dp)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "${patient.firstName} ${patient.lastName}",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.semantics { heading() },
        )
        if (patient.isArchived) AssistChip(onClick = {}, label = { Text(stringResource(badge_archived)) })

        RecordField(label_document_type, stringResource(patient.documentType.label))
        RecordField(label_document_number, patient.documentNumber)
        RecordField(label_email, patient.email)
        RecordField(label_phone, patient.phone)
        RecordField(label_address, patient.address)
        RecordField(label_notes, patient.notes)

        SignatureCard(signature = patient.signature, png = signaturePng, onSignAgain = onSignAgain)

        if (patient.hasClinicalRecords) {
            Text(
                text = stringResource(professional_has_records_notice),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        errorMessage?.let {
            ErrorMessage(stringResource(it))
        }

        FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onEdit) { Text(stringResource(button_edit)) }
            if (patient.isArchived) {
                OutlinedButton(onClick = { onIntent(PatientDetailIntent.UnarchiveClicked) }) {
                    Text(stringResource(button_unarchive))
                }
            } else {
                OutlinedButton(onClick = { onIntent(PatientDetailIntent.ArchiveClicked) }) {
                    Text(stringResource(button_archive))
                }
            }
            if (patient.canDelete) {
                TextButton(
                    onClick = { onIntent(PatientDetailIntent.DeleteClicked) },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) {
                    Text(stringResource(button_delete_permanently))
                }
            }
        }
    }
}

@Composable
private fun SignatureCard(signature: SignatureInfo?, png: ByteArray?, onSignAgain: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(signature_section_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.semantics { heading() },
        )
        val image = remember(png) { png?.let { runCatching { it.decodeToImageBitmap() }.getOrNull() } }
        if (signature == null || image == null) {
            Text(stringResource(no_signature), color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            Image(
                bitmap = image,
                contentDescription = stringResource(signature_image_description),
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .widthIn(max = 480.dp)
                    .fillMaxWidth()
                    .aspectRatio(SIGNATURE_ASPECT_RATIO)
                    .background(Color.White, MaterialTheme.shapes.medium),
            )
            Text(
                text = if (signature.signer == SignatureSigner.PATIENT) {
                    stringResource(signature_signed_by_patient)
                } else {
                    stringResource(
                        signature_signed_by_representative,
                        signature.representativeName.orEmpty(),
                        signature.representativeDocument.orEmpty(),
                    )
                },
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = stringResource(
                    signature_signed_on,
                    formatDateTime(signature.capturedAtEpochMillis),
                    signature.consentVersion,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (signature.totalSignatures > 1) {
                Text(
                    text = stringResource(signature_history_count, signature.totalSignatures),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        OutlinedButton(onClick = onSignAgain) { Text(stringResource(button_sign_again)) }
    }
}
