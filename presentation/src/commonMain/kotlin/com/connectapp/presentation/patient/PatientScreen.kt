package com.connectapp.presentation.patient

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.connectapp.designresources.TokensResources
import com.connectapp.designresources.DimensResources
import com.connectapp.domain.model.User
import com.connectapp.presentation.component.CustomOutlinedTextField
import com.connectapp.presentation.util.asString
import com.connectapp.presentation.util.formatDate
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientScreen(
    modifier: Modifier = Modifier,
    user: User,
    snackBarHostState: SnackbarHostState,
    onNavigateBack: (User) -> Unit,
    viewModel: PatientViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val onIntent = viewModel::onIntent

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                PatientEffect.NavigateBack -> onNavigateBack(user)
                is PatientEffect.ShowError -> {
                    snackBarHostState.showSnackbar(
                        message = effect.message,
                        duration = SnackbarDuration.Short
                    )

                }
            }
        }
    }

    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator()
        }
    } else {
        Box(modifier = modifier) {
            PatientScreenContent(state, onIntent)
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientScreenContent(state: PatientState, onIntent: (PatientIntent) -> Unit) {

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        // Simple manual conversion to DD/MM/AAAA
                        // In a real app, use a proper date library or helper
                        val dateString = formatDate(millis)
                        onIntent(PatientIntent.BirthDateChanged(dateString))
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
        modifier = Modifier
            .fillMaxSize()
            .padding(DimensResources.spacing16)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(DimensResources.spacing16)
    ) {
        Text(
            text = stringResource(TokensResources.personalInfo),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        CustomOutlinedTextField(
            value = state.name,
            onValueChange = { onIntent(PatientIntent.NameChanged(it)) },
            label = stringResource(TokensResources.name),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = state.nameError != null,
            supportingText = state.nameError?.let { { Text(it.asString()) } }
        )

        CustomOutlinedTextField(
            value = state.surname,
            onValueChange = { onIntent(PatientIntent.SurnameChanged(it)) },
            label = stringResource(TokensResources.surname),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = state.surnameError != null,
            supportingText = state.surnameError?.let { { Text(it.asString()) } }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(DimensResources.spacing8)
        ) {
            CustomOutlinedTextField(
                value = state.dni,
                onValueChange = { onIntent(PatientIntent.DniChanged(it)) },
                label = stringResource(TokensResources.dni),
                modifier = Modifier.weight(1f),
                singleLine = true,
                isError = state.dniError != null,
                supportingText = state.dniError?.let { { Text(it.asString()) } }
            )
            Box(modifier = Modifier.weight(1f)) {
                CustomOutlinedTextField(
                    value = state.birthDate,
                    onValueChange = { },
                    label = stringResource(TokensResources.birthDate),
                    placeHolder = stringResource(TokensResources.datePlaceholder),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    readOnly = true,
                    isError = state.birthDateError != null,
                    supportingText = state.birthDateError?.let { { Text(it.asString()) } }
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { showDatePicker = true }
                )
            }
        }

        Spacer(modifier = Modifier.height(DimensResources.spacing8))

        Text(
            text = stringResource(TokensResources.contact),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        CustomOutlinedTextField(
            value = state.email,
            onValueChange = { onIntent(PatientIntent.EmailChanged(it)) },
            label = stringResource(TokensResources.email),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            isError = state.emailError != null,
            supportingText = state.emailError?.let { { Text(it.asString()) } }
        )

        CustomOutlinedTextField(
            value = state.phone,
            onValueChange = { onIntent(PatientIntent.PhoneChanged(it)) },
            label = stringResource(TokensResources.phone),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            isError = state.phoneError != null,
            supportingText = state.phoneError?.let { { Text(it.asString()) } }
        )

        Spacer(modifier = Modifier.height(DimensResources.spacing8))

        Text(
            text = stringResource(TokensResources.additionalNotes),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        CustomOutlinedTextField(
            value = state.notes,
            onValueChange = { onIntent(PatientIntent.NotesChanged(it)) },
            label = stringResource(TokensResources.notes),
            modifier = Modifier.fillMaxWidth().height(DimensResources.spacing120),
            maxLines = 5,
            singleLine = false,
        )

        Button(
            onClick = { onIntent(PatientIntent.SaveClicked) },
            modifier = Modifier.fillMaxWidth().padding(vertical = DimensResources.spacing16),
            enabled = state.isFormValid,
            shape = MaterialTheme.shapes.large
        ) {
            Text(stringResource(TokensResources.savePatient))
        }
    }
}

