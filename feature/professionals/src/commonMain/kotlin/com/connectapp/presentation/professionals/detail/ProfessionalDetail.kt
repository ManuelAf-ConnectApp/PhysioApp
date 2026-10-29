package com.connectapp.presentation.professionals.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.connectapp.commonresources.archive_professional_message
import com.connectapp.commonresources.archive_professional_title
import com.connectapp.commonresources.badge_archived
import com.connectapp.commonresources.badge_has_account
import com.connectapp.commonresources.button_archive
import com.connectapp.commonresources.button_delete_permanently
import com.connectapp.commonresources.button_edit
import com.connectapp.commonresources.button_unarchive
import com.connectapp.commonresources.component.ConfirmationDialog
import com.connectapp.commonresources.component.EmptyState
import com.connectapp.commonresources.component.ErrorMessage
import com.connectapp.commonresources.component.LoadingContentPlaceholder
import com.connectapp.commonresources.component.RecordField
import com.connectapp.commonresources.delete_professional_message
import com.connectapp.commonresources.delete_professional_title
import com.connectapp.commonresources.label_address
import com.connectapp.commonresources.label_college
import com.connectapp.commonresources.label_email
import com.connectapp.commonresources.label_license_number
import com.connectapp.commonresources.label_nif
import com.connectapp.commonresources.label_notes
import com.connectapp.commonresources.label_phone
import com.connectapp.commonresources.professional_has_account_notice
import com.connectapp.commonresources.professional_has_records_notice
import com.connectapp.commonresources.professional_not_found
import com.connectapp.domain.professionals.Professional
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Record of one professional with its actions. Used as the detail pane of the search and as a
 * screen of its own when list and detail do not fit side by side.
 */
@Composable
fun ProfessionalDetail(
    professionalId: String,
    onEdit: (professionalId: String) -> Unit,
    onDeleted: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: ProfessionalDetailViewModel =
        koinViewModel(key = "professional-detail-$professionalId") { parametersOf(professionalId) }
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                ProfessionalDetailEffect.Deleted -> onDeleted()
            }
        }
    }

    val professional = state.professional
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        when {
            state.isLoading -> LoadingContentPlaceholder()
            professional == null -> EmptyState(
                icon = Icons.Outlined.PersonOff,
                title = stringResource(professional_not_found),
                message = "",
            )
            else -> DetailContent(
                professional = professional,
                errorMessage = state.errorMessage,
                onIntent = viewModel::onIntent,
                onEdit = { onEdit(professional.id) },
            )
        }
    }

    state.dialog?.let { dialog ->
        val archive = dialog == ProfessionalDialog.ARCHIVE
        ConfirmationDialog(
            title = if (archive) archive_professional_title else delete_professional_title,
            message = if (archive) archive_professional_message else delete_professional_message,
            confirm = if (archive) button_archive else button_delete_permanently,
            onConfirm = { viewModel.onIntent(ProfessionalDetailIntent.DialogConfirmed) },
            onDismiss = { viewModel.onIntent(ProfessionalDetailIntent.DialogDismissed) },
        )
    }
}

@Composable
private fun DetailContent(
    professional: Professional,
    errorMessage: StringResource?,
    onIntent: (ProfessionalDetailIntent) -> Unit,
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
        Text(
            text = "${professional.firstName} ${professional.lastName}",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.semantics { heading() },
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (professional.isArchived) AssistChip(onClick = {}, label = { Text(stringResource(badge_archived)) })
            if (professional.hasAccount) AssistChip(onClick = {}, label = { Text(stringResource(badge_has_account)) })
        }

        RecordField(label_nif, professional.nif)
        RecordField(label_college, professional.college.displayName)
        RecordField(label_license_number, professional.licenseNumber)
        RecordField(label_email, professional.email)
        RecordField(label_phone, professional.phone)
        RecordField(label_address, professional.address)
        RecordField(label_notes, professional.notes)

        when {
            professional.hasAccount -> Notice(professional_has_account_notice)
            professional.hasClinicalRecords -> Notice(professional_has_records_notice)
        }
        errorMessage?.let {
            ErrorMessage(stringResource(it))
        }

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(onClick = onEdit) { Text(stringResource(button_edit)) }
            when {
                professional.isArchived ->
                    OutlinedButton(onClick = { onIntent(ProfessionalDetailIntent.UnarchiveClicked) }) {
                        Text(stringResource(button_unarchive))
                    }
                professional.canArchive ->
                    OutlinedButton(onClick = { onIntent(ProfessionalDetailIntent.ArchiveClicked) }) {
                        Text(stringResource(button_archive))
                    }
            }
            if (professional.canDelete) {
                TextButton(
                    onClick = { onIntent(ProfessionalDetailIntent.DeleteClicked) },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) {
                    Text(stringResource(button_delete_permanently))
                }
            }
        }
    }
}

@Composable
private fun Notice(text: StringResource) {
    Text(
        text = stringResource(text),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
