package com.connectapp.presentation.search_patient

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.connectapp.designresources.DimensResources
import com.connectapp.designresources.TokensResources
import com.connectapp.domain.validator.model.ValidationError
import com.connectapp.presentation.component.CustomOutlinedTextField
import com.connectapp.presentation.search_patient.component.PatientDetails
import com.connectapp.presentation.search_patient.component.PatientItem
import com.connectapp.presentation.util.asString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel


@Composable
fun SearchPatientScreen(
    modifier: Modifier = Modifier,
    onNavigateToEditPatient: (String) -> Unit
) {
    val viewModel: SearchPatientViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()


    if (state.isLoading) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        Box(
            modifier = modifier.fillMaxSize().imePadding().padding(16.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            SearchPatientScreenContent(
                modifier = Modifier.fillMaxSize(),
                state = state,
                onIntent = viewModel::onIntent,
                onEditPatient = onNavigateToEditPatient
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchPatientScreenContent(
    modifier: Modifier = Modifier,
    state: SearchPatientState,
    onIntent: (SearchPatientIntent) -> Unit,
    onEditPatient: (String) -> Unit
) {
    val focusManager = LocalFocusManager.current

    LazyColumn(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            CustomOutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.email,
                onValueChange = { onIntent(SearchPatientIntent.EmailChanged(it)) },
                label = stringResource(TokensResources.email),
                enabled = state.isEmailEnabled,
                isError = state.emailError != null && state.emailError != ValidationError.FIELD_EMPTY,
                supportingText = state.emailError?.takeIf { it != ValidationError.FIELD_EMPTY }
                    ?.let { error ->
                        { Text(text = error.asString()) }
                    },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        if (state.canSearch && !state.isLoading) {
                            onIntent(SearchPatientIntent.SearchClicked)
                            focusManager.clearFocus()
                        }
                    }
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            CustomOutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.phone,
                onValueChange = { onIntent(SearchPatientIntent.PhoneChanged(it)) },
                label = stringResource(TokensResources.phone),
                enabled = state.isPhoneEnabled,
                isError = state.phoneError != null && state.phoneError != ValidationError.FIELD_EMPTY,
                supportingText = state.phoneError?.takeIf { it != ValidationError.FIELD_EMPTY }
                    ?.let { error ->
                        { Text(text = error.asString()) }
                    },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        if (state.canSearch && !state.isLoading) {
                            onIntent(SearchPatientIntent.SearchClicked)
                            focusManager.clearFocus()
                        }
                    }
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            CustomOutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.dni,
                onValueChange = { onIntent(SearchPatientIntent.DNIChanged(it)) },
                label = stringResource(TokensResources.dni),
                enabled = state.isDNIEnabled,
                isError = state.dniError != null && state.dniError != ValidationError.FIELD_EMPTY,
                supportingText = state.dniError?.takeIf { it != ValidationError.FIELD_EMPTY }
                    ?.let { error ->
                        { Text(text = error.asString()) }
                    },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        if (state.canSearch && !state.isLoading) {
                            onIntent(SearchPatientIntent.SearchClicked)
                            focusManager.clearFocus()
                        }
                    }
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { onIntent(SearchPatientIntent.SearchClicked) },
                enabled = state.canSearch && !state.isLoading &&
                        (state.emailError == null || state.emailError == ValidationError.FIELD_EMPTY) &&
                        (state.phoneError == null || state.phoneError == ValidationError.FIELD_EMPTY) &&
                        (state.dniError == null || state.dniError == ValidationError.FIELD_EMPTY),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(TokensResources.searchPatient))
            }

            Spacer(modifier = Modifier.height(16.dp))

            state.error?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }
        }

        item {
            if (state.results.isNotEmpty()) {
                Spacer(modifier = Modifier.height(DimensResources.spacing8))

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = DimensResources.spacing8),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = { onIntent(SearchPatientIntent.ClearSearch) },
                    ) {
                        Text(
                            "Limpiar búsquedas",
                            textAlign = TextAlign.End
                        )

                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Clear",
                        )
                    }

                }

            }
        }

        items(state.results) { patient ->
            PatientItem(
                patient = patient,
                onClick = { onIntent(SearchPatientIntent.PatientSelected(patient)) }
            )
        }

        item {
            state.selectedPatient?.let { patient ->
                Spacer(modifier = Modifier.height(DimensResources.spacing32))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(DimensResources.spacing16))
                PatientDetails(
                    patient = patient,
                    onEditClick = { onEditPatient(patient.dni) }
                )
            }
        }
    }
}