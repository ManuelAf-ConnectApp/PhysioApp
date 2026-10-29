package com.connectapp.presentation.reports.form

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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.connectapp.commonresources.button_save
import com.connectapp.commonresources.component.ErrorMessage
import com.connectapp.commonresources.component.LoadingIndicator
import com.connectapp.commonresources.component.LoadingContentPlaceholder
import com.connectapp.commonresources.component.PatientPicker
import com.connectapp.commonresources.component.PatientPickerState
import com.connectapp.commonresources.component.PickerItem
import com.connectapp.commonresources.component.RecordField
import com.connectapp.commonresources.component.ValidatedTextField
import com.connectapp.commonresources.error_required_field
import com.connectapp.commonresources.label_condition_text
import com.connectapp.commonresources.label_patient
import com.connectapp.commonresources.label_professional
import com.connectapp.commonresources.label_report_text
import com.connectapp.commonresources.navigation.GuardUnsavedChanges
import com.connectapp.domain.reports.ReportField
import com.connectapp.domain.shared.patient.PatientOption
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/** Create (no [reportId]) or edit a clinical report. */
@Composable
fun ReportFormScreen(
    reportId: String?,
    onSaved: (reportId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: ReportFormViewModel = koinViewModel { parametersOf(reportId) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val onIntent = viewModel::onIntent

    GuardUnsavedChanges(dirty = state.hasUnsavedChanges)

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ReportFormEffect.Saved -> onSaved(effect.reportId)
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
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            state.blockedReason?.let {
                Text(
                    text = stringResource(it),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            RecordField(label_professional, state.authorName.ifBlank { null })
            val fixedPatient = state.fixedPatientLabel
            if (fixedPatient != null) {
                RecordField(label_patient, fixedPatient)
            } else {
                PatientPicker(
                    state = PatientPickerState(
                        query = state.patientQuery,
                        suggestions = state.suggestions.map { it.toPickerItem() },
                        selected = state.selectedPatient?.toPickerItem(),
                    ),
                    onQueryChange = { onIntent(ReportFormIntent.PatientQueryChanged(it)) },
                    onSelect = { id ->
                        val patient = state.suggestions.firstOrNull { it.id == id }
                        if (patient != null) onIntent(ReportFormIntent.PatientSelected(patient))
                    },
                    onClear = { onIntent(ReportFormIntent.PatientCleared) },
                )
            }
            ValidatedTextField(
                value = state.reportText,
                onValueChange = { onIntent(ReportFormIntent.ReportTextChanged(it)) },
                label = label_report_text,
                error = error_required_field.takeIf { ReportField.REPORT_TEXT in state.invalidFields },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                minLines = 6,
                modifier = Modifier.fillMaxWidth(),
            )
            ValidatedTextField(
                value = state.conditionText,
                onValueChange = { onIntent(ReportFormIntent.ConditionTextChanged(it)) },
                label = label_condition_text,
                error = error_required_field.takeIf { ReportField.CONDITION_TEXT in state.invalidFields },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                minLines = 4,
                modifier = Modifier.fillMaxWidth(),
            )
            state.errorMessage?.let {
                ErrorMessage(stringResource(it))
            }
            Spacer(modifier = Modifier.height(4.dp))
            if (state.isSaving) {
                LoadingIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (state.blockedReason == null) {
                Button(
                    onClick = { onIntent(ReportFormIntent.SaveClicked) },
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

private fun PatientOption.toPickerItem() = PickerItem(id, "$firstName $lastName", documentNumber)
