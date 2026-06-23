package com.connectapp.presentation.report

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.connectapp.designresources.DimensResources
import com.connectapp.designresources.TokensResources
import com.connectapp.presentation.component.CustomOutlinedTextField
import com.connectapp.presentation.util.formatDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.todayIn
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock
import org.koin.compose.viewmodel.koinViewModel


@Composable
fun ReportScreen(
    modifier: Modifier = Modifier,
    reportId: String? = null,
    snackBarHostState: SnackbarHostState,
    onNavigateBack: () -> Unit,
    onNavigateToCreatePatient: () -> Unit,
) {
    val viewModel: ReportViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()
    val onIntent = viewModel::onIntent

    val isEditMode = reportId != null

    LaunchedEffect(reportId) {
        reportId?.let {
            onIntent(ReportIntent.Initialize(reportId = it))
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                ReportEffect.NavigateBack -> onNavigateBack()
                ReportEffect.NavigateToCreatePatient -> onNavigateToCreatePatient()
                is ReportEffect.ShowError -> {
                    snackBarHostState.showSnackbar(effect.message)
                }

                is ReportEffect.ShowMessage -> {
                    snackBarHostState.showSnackbar(effect.message)
                    onNavigateBack()
                }
            }
        }
    }

    if (state.isLoading) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
    } else {
        ReportScreenContent(
            state = state,
            onIntent = onIntent,
            isEditMode = isEditMode,
            modifier = modifier
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreenContent(
    state: ReportState,
    onIntent: (ReportIntent) -> Unit,
    isEditMode: Boolean,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var expandedProfessionals by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val todayStartUtc = Clock.System.todayIn(TimeZone.currentSystemDefault())
                    .atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
                return utcTimeMillis >= todayStartUtc
            }
        }
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val dateString = formatDate(millis)
                        onIntent(ReportIntent.DateChanged(dateString))
                    }
                    showDatePicker = false
                }) {
                    Text(stringResource(TokensResources.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(TokensResources.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(DimensResources.spacing16)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(DimensResources.spacing16)
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(TokensResources.reportDetails),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        CustomOutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = state.reportTitle,
            onValueChange = { onIntent(ReportIntent.ReportTitleChanged(it)) },
            label = stringResource(TokensResources.reportTitleLabel),
            placeHolder = stringResource(TokensResources.reportTitlePlaceholder),
            singleLine = true
        )

        ExposedDropdownMenuBox(
            expanded = expandedProfessionals,
            onExpandedChange = { expandedProfessionals = !expandedProfessionals },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth().menuAnchor(
                    ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                    true
                ),
                readOnly = true,
                value = state.selectedProfessional?.let { "${it.firstName} ${it.lastName}" } ?: "",
                onValueChange = {},
                label = { Text(stringResource(TokensResources.professional)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedProfessionals) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                shape = MaterialTheme.shapes.large
            )
            ExposedDropdownMenu(
                expanded = expandedProfessionals,
                onDismissRequest = { expandedProfessionals = false }
            ) {
                state.professionals.forEach { professional ->
                    DropdownMenuItem(
                        text = { Text("${professional.firstName} ${professional.lastName}") },
                        onClick = {
                            onIntent(ReportIntent.ProfessionalSelected(professional))
                            expandedProfessionals = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }

        Box(modifier = Modifier.fillMaxWidth().zIndex(1f)) {
            Column {
                CustomOutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = state.patientName,
                    onValueChange = { onIntent(ReportIntent.PatientNameChanged(it)) },
                    label = stringResource(TokensResources.patientName),
                    singleLine = true
                )

                if (state.suggestedPatients.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = DimensResources.spacing4),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column {
                            state.suggestedPatients.forEach { patient ->
                                Text(
                                    text = "${patient.firstName} ${patient.lastName}",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onIntent(ReportIntent.PatientSelected(patient)) }
                                        .padding(DimensResources.spacing16),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = DimensResources.spacing16),
                                    thickness = DimensResources.thickness05,
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                            }
                        }
                    }
                } else if (state.patientName.length >= 3 && !state.isLoading && state.selectedPatient == null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = DimensResources.spacing4),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Text(
                            text = stringResource(TokensResources.newPatient),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onIntent(ReportIntent.AddPatientClicked) }
                                .padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Box(modifier = Modifier.fillMaxWidth()) {
            CustomOutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.date,
                onValueChange = { },
                label = stringResource(TokensResources.dateFormat),
                singleLine = true,
                readOnly = true
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { showDatePicker = true }
            )
        }

        Spacer(modifier = Modifier.height(DimensResources.spacing8))

        Text(
            text = stringResource(TokensResources.labelClinicalContent),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        CustomOutlinedTextField(
            value = state.clinicalContent,
            onValueChange = { onIntent(ReportIntent.ClinicalContentChanged(it)) },
            label = stringResource(TokensResources.labelClinicalContent),
            modifier = Modifier.fillMaxWidth(),
            singleLine = false,
            minLines = 5
        )

        CustomOutlinedTextField(
            value = state.diagnosis,
            onValueChange = { onIntent(ReportIntent.DiagnosisChanged(it)) },
            label = stringResource(TokensResources.labelDiagnosis),
            modifier = Modifier.fillMaxWidth(),
            singleLine = false,
            minLines = 3
        )

        CustomOutlinedTextField(
            value = state.treatment,
            onValueChange = { onIntent(ReportIntent.TreatmentChanged(it)) },
            label = stringResource(TokensResources.labelTreatment),
            modifier = Modifier.fillMaxWidth(),
            singleLine = false,
            minLines = 5
        )

        Button(
            onClick = { onIntent(ReportIntent.SaveClicked) },
            modifier = Modifier.fillMaxWidth().padding(vertical = DimensResources.spacing16),
            enabled = state.isFormValid,
            shape = MaterialTheme.shapes.large
        ) {
            Text(if (isEditMode) stringResource(TokensResources.updateReport) else stringResource(TokensResources.saveReport))
        }
    }
}