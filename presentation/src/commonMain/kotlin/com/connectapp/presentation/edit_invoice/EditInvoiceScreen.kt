package com.connectapp.presentation.edit_invoice

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.connectapp.designresources.TokensResources
import com.connectapp.designresources.DimensResources
import com.connectapp.presentation.component.CustomOutlinedTextField
import com.connectapp.presentation.util.formatDate
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun EditInvoiceScreen(
    modifier: Modifier = Modifier,
    invoiceId: String,
    snackBarHostState: SnackbarHostState,
    onNavigateBack: () -> Unit,
) {
    val viewModel: EditInvoiceViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val onIntent = viewModel::onIntent

    LaunchedEffect(invoiceId) {
        onIntent(EditInvoiceIntent.LoadInvoice(invoiceId))
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                EditInvoiceEffect.NavigateBack -> onNavigateBack()
                is EditInvoiceEffect.ShowError -> {
                    snackBarHostState.showSnackbar(
                        message = effect.message,
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        EditInvoiceScreenContent(
            modifier = modifier,
            state = state,
            onIntent = onIntent,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditInvoiceScreenContent(
    modifier: Modifier = Modifier,
    state: EditInvoiceState,
    onIntent: (EditInvoiceIntent) -> Unit,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    var expandedProfessional by remember { mutableStateOf(false) }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val dateString = formatDate(millis)
                        onIntent(EditInvoiceIntent.DateChanged(dateString))
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
            .statusBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(DimensResources.spacing16),
        verticalArrangement = Arrangement.spacedBy(DimensResources.spacing16)
    ) {
        Text(
            text = stringResource(TokensResources.editInvoice),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        CustomOutlinedTextField(
            value = state.invoiceNumber,
            onValueChange = { },
            label = stringResource(TokensResources.invoiceNumber),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            readOnly = true,
            enabled = false,
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        )

        ExposedDropdownMenuBox(
            expanded = expandedProfessional,
            onExpandedChange = { expandedProfessional = !expandedProfessional },
            modifier = Modifier.fillMaxWidth()
        ) {
            val selectedProfessionalName = state.professionals.find { it.id.toString() == state.professionalId }?.let {
                "${it.firstName} ${it.lastName}"
            } ?: ""

            CustomOutlinedTextField(
                value = selectedProfessionalName,
                onValueChange = { },
                label = stringResource(TokensResources.professional),
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                    .fillMaxWidth(),
                singleLine = true,
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedProfessional) }
            )

            ExposedDropdownMenu(
                expanded = expandedProfessional,
                onDismissRequest = { expandedProfessional = false }
            ) {
                state.professionals.forEach { professional ->
                    DropdownMenuItem(
                        text = { Text("${professional.firstName} ${professional.lastName}") },
                        onClick = {
                            onIntent(EditInvoiceIntent.ProfessionalSelected(professional.id.toString()))
                            expandedProfessional = false
                        }
                    )
                }
            }
        }

        Box(modifier = Modifier.fillMaxWidth().zIndex(1f)) {
            Column {
                CustomOutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = state.patientName,
                    onValueChange = { onIntent(EditInvoiceIntent.PatientNameChanged(it)) },
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
                                        .clickable { onIntent(EditInvoiceIntent.PatientSelected(patient)) }
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

        CustomOutlinedTextField(
            value = state.concept,
            onValueChange = { onIntent(EditInvoiceIntent.ConceptChanged(it)) },
            label = stringResource(TokensResources.concept),
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        CustomOutlinedTextField(
            value = state.amount,
            onValueChange = { onIntent(EditInvoiceIntent.AmountChanged(it)) },
            label = stringResource(TokensResources.amountEuro),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(TokensResources.isPaid),
                style = MaterialTheme.typography.bodyLarge
            )
            Switch(
                checked = state.isPaid,
                onCheckedChange = { onIntent(EditInvoiceIntent.PaidStatusChanged(it)) }
            )
        }

        Button(
            onClick = { onIntent(EditInvoiceIntent.SaveClicked) },
            modifier = Modifier.fillMaxWidth().padding(vertical = DimensResources.spacing16),
            enabled = !state.isLoading,
            shape = MaterialTheme.shapes.large
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(DimensResources.spacing24),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(stringResource(TokensResources.generateInvoice))
            }
        }
    }
}
