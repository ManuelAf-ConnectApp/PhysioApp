package com.connectapp.presentation.professionals.form

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
import com.connectapp.commonresources.component.SelectField
import com.connectapp.commonresources.component.ValidatedTextField
import com.connectapp.commonresources.error_invalid_email
import com.connectapp.commonresources.error_invalid_license_number
import com.connectapp.commonresources.error_invalid_nif
import com.connectapp.commonresources.error_invalid_phone
import com.connectapp.commonresources.error_required_field
import com.connectapp.commonresources.error_select_college
import com.connectapp.commonresources.label_address
import com.connectapp.commonresources.label_college
import com.connectapp.commonresources.label_email
import com.connectapp.commonresources.label_first_name
import com.connectapp.commonresources.label_last_name
import com.connectapp.commonresources.label_license_number
import com.connectapp.commonresources.label_nif
import com.connectapp.commonresources.label_notes
import com.connectapp.commonresources.label_phone
import com.connectapp.commonresources.license_number_hint
import com.connectapp.commonresources.navigation.GuardUnsavedChanges
import com.connectapp.commonresources.optional_hint
import com.connectapp.domain.professionals.ProfessionalDraft
import com.connectapp.domain.professionals.ProfessionalField
import com.connectapp.domain.shared.professional.ProfessionalCollege
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/** Create (no [professionalId]) or edit a professional record. */
@Composable
fun ProfessionalFormScreen(
    professionalId: String?,
    onSaved: (professionalId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: ProfessionalFormViewModel = koinViewModel { parametersOf(professionalId) }
    val state by viewModel.state.collectAsStateWithLifecycle()

    GuardUnsavedChanges(dirty = state.hasUnsavedChanges)

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ProfessionalFormEffect.Saved -> onSaved(effect.professionalId)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        if (state.isLoading) {
            LoadingIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            Column(
                modifier = Modifier
                    .widthIn(max = 720.dp)
                    .fillMaxSize()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ProfessionalForm(
                    draft = state.draft,
                    invalid = state.invalidFields,
                    onChange = { viewModel.onIntent(ProfessionalFormIntent.DraftChanged(it)) },
                )
                Spacer(modifier = Modifier.height(8.dp))
                state.errorMessage?.let {
                    ErrorMessage(stringResource(it))
                }
                if (state.isSaving) {
                    LoadingIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else {
                    Button(
                        onClick = { viewModel.onIntent(ProfessionalFormIntent.SaveClicked) },
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
}

@Composable
private fun ProfessionalForm(
    draft: ProfessionalDraft,
    invalid: Set<ProfessionalField>,
    onChange: (ProfessionalDraft) -> Unit,
) {
    val fieldModifier = Modifier.fillMaxWidth()
    val upperCase = KeyboardOptions(capitalization = KeyboardCapitalization.Characters)
    val words = KeyboardOptions(capitalization = KeyboardCapitalization.Words)

    ValidatedTextField(
        value = draft.nif,
        onValueChange = { onChange(draft.copy(nif = it)) },
        label = label_nif,
        error = error_invalid_nif.takeIf { ProfessionalField.NIF in invalid },
        keyboardOptions = upperCase,
        modifier = fieldModifier,
    )
    SelectField(
        selected = draft.college?.displayName,
        options = ProfessionalCollege.entries.map { it.displayName },
        onSelected = { onChange(draft.copy(college = ProfessionalCollege.entries[it])) },
        label = label_college,
        error = error_select_college.takeIf { ProfessionalField.COLLEGE in invalid },
        modifier = fieldModifier,
    )
    ValidatedTextField(
        value = draft.licenseNumber,
        onValueChange = { onChange(draft.copy(licenseNumber = it)) },
        label = label_license_number,
        error = error_invalid_license_number.takeIf { ProfessionalField.LICENSE_NUMBER in invalid },
        supportingText = license_number_hint,
        keyboardOptions = upperCase,
        modifier = fieldModifier,
    )
    ValidatedTextField(
        value = draft.firstName,
        onValueChange = { onChange(draft.copy(firstName = it)) },
        label = label_first_name,
        error = error_required_field.takeIf { ProfessionalField.FIRST_NAME in invalid },
        keyboardOptions = words,
        modifier = fieldModifier,
    )
    ValidatedTextField(
        value = draft.lastName,
        onValueChange = { onChange(draft.copy(lastName = it)) },
        label = label_last_name,
        error = error_required_field.takeIf { ProfessionalField.LAST_NAME in invalid },
        keyboardOptions = words,
        modifier = fieldModifier,
    )
    ValidatedTextField(
        value = draft.email,
        onValueChange = { onChange(draft.copy(email = it)) },
        label = label_email,
        error = error_invalid_email.takeIf { ProfessionalField.EMAIL in invalid },
        supportingText = optional_hint,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        modifier = fieldModifier,
    )
    ValidatedTextField(
        value = draft.phone,
        onValueChange = { onChange(draft.copy(phone = it)) },
        label = label_phone,
        error = error_invalid_phone.takeIf { ProfessionalField.PHONE in invalid },
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
