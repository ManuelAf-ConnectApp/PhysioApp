package com.connectapp.presentation.professional

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.connectapp.designresources.TokensResources
import com.connectapp.designresources.DimensResources
import com.connectapp.presentation.component.CustomOutlinedTextField
import com.connectapp.presentation.util.asString
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProfessionalScreen(
    modifier: Modifier = Modifier,
    snackBarHostState: SnackbarHostState,
    onNavigateBack: () -> Unit,
) {
    val viewModel: ProfessionalViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()
    val onIntent = viewModel::onIntent

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                ProfessionalEffect.NavigateBack -> onNavigateBack()
                is ProfessionalEffect.ShowError -> { /* Handle error */
                }

                is ProfessionalEffect.SaveFailure -> {
                    snackBarHostState.showSnackbar(
                        message = effect.error,
                        duration = SnackbarDuration.Short
                    )
                }

                ProfessionalEffect.SaveSuccess -> {
                    snackBarHostState.showSnackbar(
                        message = getString(TokensResources.professionalSaved),
                        duration = SnackbarDuration.Short
                    )
                    onNavigateBack()
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
            ProfessionalScreenContent(state, onIntent)
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfessionalScreenContent(state: ProfessionalState, onIntent: (ProfessionalIntent) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(DimensResources.spacing16)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(DimensResources.spacing16)
    ) {
        Text(
            text = stringResource(TokensResources.professionalInfo),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )


        CustomOutlinedTextField(
            value = state.name,
            onValueChange = { onIntent(ProfessionalIntent.NameChanged(it)) },
            label = stringResource(TokensResources.name),
            placeHolder = stringResource(TokensResources.name),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = state.nameError != null,
            supportingText = state.nameError?.let { { Text(it.asString()) } }
        )


        CustomOutlinedTextField(
            value = state.surname,
            onValueChange = { onIntent(ProfessionalIntent.SurnameChanged(it)) },
            label = stringResource(TokensResources.surname),
            placeHolder = stringResource(TokensResources.surname),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = state.surnameError != null,
            supportingText = state.surnameError?.let { { Text(it.asString()) } }
        )


        Column(modifier = Modifier.fillMaxWidth()) {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                val selectedSpecialtyName =
                    state.specialties.find { it.id == state.specialty }?.let {
                        val resource = when(it.name) {
                            "specialty_musculoskeletal" -> TokensResources.specialtyMusculoskeletal
                            "specialty_sports" -> TokensResources.specialtySports
                            "specialty_neurological" -> TokensResources.specialtyNeurological
                            "specialty_respiratory" -> TokensResources.specialtyRespiratory
                            "specialty_pediatric" -> TokensResources.specialtyPediatric
                            "specialty_geriatric" -> TokensResources.specialtyGeriatric
                            "specialty_pelvic" -> TokensResources.specialtyPelvic
                            "specialty_cardiovascular" -> TokensResources.specialtyCardiovascular
                            "specialty_oncological" -> TokensResources.specialtyOncological
                            "specialty_palliative" -> TokensResources.specialtyPalliative
                            "specialty_chronic_pain" -> TokensResources.specialtyChronicPain
                            "specialty_occupational" -> TokensResources.specialtyOccupational
                            "specialty_manual_therapy" -> TokensResources.specialtyManualTherapy
                            "specialty_invasive" -> TokensResources.specialtyInvasive
                            else -> null
                        }
                        if (resource != null) stringResource(resource) else it.name
                    } ?: ""
                OutlinedTextField(
                    value = selectedSpecialtyName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(TokensResources.specialty)) },
                    placeholder = { Text(stringResource(TokensResources.specialtyPlaceholder)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                    ),
                    isError = state.specialtyError != null,
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier.menuAnchor(
                        ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                        true
                    ).fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    state.specialties.forEach { specialty ->
                        val specialtyName = when(specialty.name) {
                            "specialty_musculoskeletal" -> stringResource(TokensResources.specialtyMusculoskeletal)
                            "specialty_sports" -> stringResource(TokensResources.specialtySports)
                            "specialty_neurological" -> stringResource(TokensResources.specialtyNeurological)
                            "specialty_respiratory" -> stringResource(TokensResources.specialtyRespiratory)
                            "specialty_pediatric" -> stringResource(TokensResources.specialtyPediatric)
                            "specialty_geriatric" -> stringResource(TokensResources.specialtyGeriatric)
                            "specialty_pelvic" -> stringResource(TokensResources.specialtyPelvic)
                            "specialty_cardiovascular" -> stringResource(TokensResources.specialtyCardiovascular)
                            "specialty_oncological" -> stringResource(TokensResources.specialtyOncological)
                            "specialty_palliative" -> stringResource(TokensResources.specialtyPalliative)
                            "specialty_chronic_pain" -> stringResource(TokensResources.specialtyChronicPain)
                            "specialty_occupational" -> stringResource(TokensResources.specialtyOccupational)
                            "specialty_manual_therapy" -> stringResource(TokensResources.specialtyManualTherapy)
                            "specialty_invasive" -> stringResource(TokensResources.specialtyInvasive)
                            else -> specialty.name
                        }
                        DropdownMenuItem(
                            text = { Text(specialtyName) },
                            onClick = {
                                onIntent(ProfessionalIntent.SpecialtyChanged(specialty.id))
                                expanded = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }
            state.specialtyError?.let {
                Text(
                    text = it.asString(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = DimensResources.spacing16, top = DimensResources.spacing4)
                )
            }
        }

        CustomOutlinedTextField(
            value = state.collegiateNumber,
            onValueChange = { onIntent(ProfessionalIntent.CollegiateNumberChanged(it)) },
            label = stringResource(TokensResources.collegiateNumber),
            placeHolder = stringResource(TokensResources.collegiateNumberPlaceholder),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = state.collegiateNumberError != null,
            supportingText = state.collegiateNumberError?.let { { Text(it.asString()) } }
        )

        Text(
            text = stringResource(TokensResources.contact),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        CustomOutlinedTextField(
            value = state.email,
            onValueChange = { onIntent(ProfessionalIntent.EmailChanged(it)) },
            label = stringResource(TokensResources.professionalEmail),
            placeHolder = stringResource(TokensResources.professionalEmail),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            isError = state.emailError != null,
            supportingText = state.emailError?.let { { Text(it.asString()) } }
        )

        CustomOutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = state.phone,
            onValueChange = { onIntent(ProfessionalIntent.PhoneChanged(it)) },
            label = stringResource(TokensResources.phone),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            isError = state.phoneError != null,
            supportingText = state.phoneError?.let { { Text(it.asString()) } }
        )


        CustomOutlinedTextField(
            modifier = Modifier.fillMaxWidth().height(DimensResources.spacing120),
            value = state.notes,
            onValueChange = { onIntent(ProfessionalIntent.NotesChanged(it)) },
            label = stringResource(TokensResources.notes),
            maxLines = 5,
            singleLine = false,
        )

        Spacer(modifier = Modifier.height(DimensResources.spacing8))

        Button(
            onClick = { onIntent(ProfessionalIntent.SaveClicked) },
            modifier = Modifier.fillMaxWidth().padding(vertical = DimensResources.spacing16),
            enabled = state.isFormValid,
            shape = MaterialTheme.shapes.large
        ) {
            Text(stringResource(TokensResources.saveProfessional))
        }
    }
}
