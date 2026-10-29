package com.connectapp.presentation.patients.form

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.connectapp.commonresources.button_save
import com.connectapp.commonresources.component.ErrorMessage
import com.connectapp.commonresources.component.LoadingIndicator
import com.connectapp.commonresources.component.LoadingContentPlaceholder
import com.connectapp.commonresources.component.SelectField
import com.connectapp.commonresources.component.ValidatedTextField
import com.connectapp.commonresources.document_other_hint
import com.connectapp.commonresources.error_invalid_document
import com.connectapp.commonresources.error_invalid_email
import com.connectapp.commonresources.error_invalid_phone
import com.connectapp.commonresources.error_required_field
import com.connectapp.commonresources.label_address
import com.connectapp.commonresources.label_document_number
import com.connectapp.commonresources.label_document_type
import com.connectapp.commonresources.label_email
import com.connectapp.commonresources.label_first_name
import com.connectapp.commonresources.label_last_name
import com.connectapp.commonresources.label_notes
import com.connectapp.commonresources.label_phone
import com.connectapp.commonresources.navigation.GuardUnsavedChanges
import com.connectapp.commonresources.optional_hint
import com.connectapp.commonresources.signature.renderSignaturePng
import com.connectapp.domain.patients.PatientDraft
import com.connectapp.domain.patients.PatientField
import com.connectapp.domain.shared.validation.IdentityDocumentType
import com.connectapp.presentation.patients.consent.ConsentSection
import com.connectapp.presentation.patients.label
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/** Register (no [patientId], with the consent signature) or edit a patient. */
@Composable
fun PatientFormScreen(
    patientId: String?,
    onSaved: (patientId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: PatientFormViewModel = koinViewModel { parametersOf(patientId) }
    val state by viewModel.state.collectAsStateWithLifecycle()

    GuardUnsavedChanges(dirty = state.hasUnsavedChanges)

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is PatientFormEffect.Saved -> onSaved(effect.patientId)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        if (state.isLoading) {
            LoadingContentPlaceholder()
            return@Box
        }
        Column(
            modifier = Modifier
                .widthIn(max = 720.dp)
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PatientFields(
                draft = state.draft,
                invalid = state.invalidFields,
                onChange = { viewModel.onIntent(PatientFormIntent.DraftChanged(it)) },
            )
            if (!state.isEditing) {
                Spacer(modifier = Modifier.height(16.dp))
                ConsentSection(
                    state = state.consent,
                    onChange = { viewModel.onIntent(PatientFormIntent.ConsentChanged(it)) },
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            state.errorMessage?.let {
                ErrorMessage(stringResource(it))
            }
            if (state.isSaving) {
                LoadingIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                Button(
                    onClick = {
                        val png = if (state.isEditing) ByteArray(0) else renderSignaturePng(state.consent.strokes)
                        viewModel.onIntent(PatientFormIntent.SaveClicked(png))
                    },
                    enabled = state.canSubmit,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                    shape = MaterialTheme.shapes.large,
                ) {
                    Text(stringResource(button_save), style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
private fun PatientFields(draft: PatientDraft, invalid: Set<PatientField>, onChange: (PatientDraft) -> Unit) {
    val fieldModifier = Modifier.fillMaxWidth()
    val words = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
    val documentTypes = IdentityDocumentType.entries

    SelectField(
        selected = stringResource(draft.documentType.label),
        options = documentTypes.map { stringResource(it.label) },
        onSelected = { onChange(draft.copy(documentType = documentTypes[it])) },
        label = label_document_type,
        error = null,
        modifier = fieldModifier,
    )
    ValidatedTextField(
        value = draft.documentNumber,
        onValueChange = { onChange(draft.copy(documentNumber = it)) },
        label = label_document_number,
        error = error_invalid_document.takeIf { PatientField.DOCUMENT_NUMBER in invalid },
        supportingText = document_other_hint.takeIf { draft.documentType == IdentityDocumentType.OTHER },
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
        modifier = fieldModifier,
    )
    ValidatedTextField(
        value = draft.firstName,
        onValueChange = { onChange(draft.copy(firstName = it)) },
        label = label_first_name,
        error = error_required_field.takeIf { PatientField.FIRST_NAME in invalid },
        keyboardOptions = words,
        modifier = fieldModifier,
    )
    ValidatedTextField(
        value = draft.lastName,
        onValueChange = { onChange(draft.copy(lastName = it)) },
        label = label_last_name,
        error = error_required_field.takeIf { PatientField.LAST_NAME in invalid },
        keyboardOptions = words,
        modifier = fieldModifier,
    )
    ValidatedTextField(
        value = draft.email,
        onValueChange = { onChange(draft.copy(email = it)) },
        label = label_email,
        error = error_invalid_email.takeIf { PatientField.EMAIL in invalid },
        supportingText = optional_hint,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        modifier = fieldModifier,
    )
    ValidatedTextField(
        value = draft.phone,
        onValueChange = { onChange(draft.copy(phone = it)) },
        label = label_phone,
        error = error_invalid_phone.takeIf { PatientField.PHONE in invalid },
        supportingText = optional_hint,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        modifier = fieldModifier,
    )
    ValidatedTextField(
        value = draft.address,
        onValueChange = { onChange(draft.copy(address = it)) },
        label = label_address,
        error = null,
        supportingText = optional_hint,
        keyboardOptions = words,
        minLines = 2,
        modifier = fieldModifier,
    )
    ValidatedTextField(
        value = draft.notes,
        onValueChange = { onChange(draft.copy(notes = it)) },
        label = label_notes,
        error = null,
        supportingText = optional_hint,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        minLines = 3,
        modifier = fieldModifier,
    )
}
