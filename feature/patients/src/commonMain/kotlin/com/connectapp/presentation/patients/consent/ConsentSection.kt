package com.connectapp.presentation.patients.consent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.connectapp.commonresources.button_clear_signature
import com.connectapp.commonresources.component.ValidatedTextField
import com.connectapp.commonresources.consent_pending_review
import com.connectapp.commonresources.consent_section_title
import com.connectapp.commonresources.consent_text
import com.connectapp.commonresources.error_invalid_document
import com.connectapp.commonresources.error_required_field
import com.connectapp.commonresources.error_signature_required
import com.connectapp.commonresources.label_representative_document
import com.connectapp.commonresources.label_representative_name
import com.connectapp.commonresources.label_signer
import com.connectapp.commonresources.signature.SignaturePad
import com.connectapp.commonresources.signature_pad_description
import com.connectapp.commonresources.signature_pad_label
import com.connectapp.commonresources.signer_patient
import com.connectapp.commonresources.signer_representative
import com.connectapp.domain.patients.Consent
import com.connectapp.domain.patients.PatientField
import com.connectapp.domain.patients.SignatureSigner
import org.jetbrains.compose.resources.stringResource

/**
 * The consent text, the version it carries, who signs and the signature pad with "Limpiar"
 * (D13). The text is shown in full so the signature always refers to it.
 */
@Composable
fun ConsentSection(
    state: ConsentFormState,
    onChange: (ConsentFormState) -> Unit,
    modifier: Modifier = Modifier,
) {
    val invalid = state.invalidFields
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(consent_section_title),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.semantics { heading() },
        )
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(consent_pending_review),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error,
                )
                Text(stringResource(consent_text), style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = Consent.CURRENT_VERSION,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Text(stringResource(label_signer), style = MaterialTheme.typography.labelLarge)
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            val signers = SignatureSigner.entries
            signers.forEachIndexed { index, signer ->
                SegmentedButton(
                    selected = state.signer == signer,
                    onClick = { onChange(state.copy(signer = signer)) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = signers.size),
                    label = {
                        val label = if (signer == SignatureSigner.PATIENT) signer_patient else signer_representative
                        Text(stringResource(label))
                    },
                )
            }
        }
        if (state.signer == SignatureSigner.REPRESENTATIVE) {
            ValidatedTextField(
                value = state.representativeName,
                onValueChange = { onChange(state.copy(representativeName = it)) },
                label = label_representative_name,
                error = error_required_field.takeIf { PatientField.REPRESENTATIVE_NAME in invalid },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                modifier = Modifier.fillMaxWidth(),
            )
            ValidatedTextField(
                value = state.representativeDocument,
                onValueChange = { onChange(state.copy(representativeDocument = it)) },
                label = label_representative_document,
                error = error_invalid_document.takeIf { PatientField.REPRESENTATIVE_DOCUMENT in invalid },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(stringResource(signature_pad_label), style = MaterialTheme.typography.labelLarge)
            TextButton(onClick = { onChange(state.copy(strokes = emptyList())) }, enabled = state.hasDrawing) {
                Text(stringResource(button_clear_signature))
            }
        }
        SignaturePad(
            strokes = state.strokes,
            onStrokesChange = { onChange(state.copy(strokes = it)) },
            contentDescription = stringResource(signature_pad_description),
        )
        // The save button stays disabled until there is a signature; this says why.
        if (PatientField.SIGNATURE in invalid) {
            Text(
                text = stringResource(error_signature_required),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
