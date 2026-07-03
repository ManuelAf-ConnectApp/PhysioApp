package com.connectapp.presentation.search_professional

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.connectapp.designresources.DimensResources
import com.connectapp.designresources.TokensResources
import com.connectapp.domain.validator.model.ValidationError
import com.connectapp.presentation.component.CustomOutlinedTextField
import com.connectapp.presentation.model.ProfessionalUi
import com.connectapp.presentation.search_professional.component.ProfessionalItem
import com.connectapp.presentation.util.asString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SearchProfessionalScreen(
    modifier: Modifier = Modifier,
) {
    val viewModel: SearchProfessionalViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val onIntent = viewModel::onIntent


    if (state.isLoading) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        Box(
            modifier = modifier.fillMaxSize().imePadding(),
            contentAlignment = Alignment.Center
        ) {
            SearchProfessionalScreenContent(
                modifier = Modifier.fillMaxSize()
                    .padding(DimensResources.spacing16, vertical = DimensResources.spacing24),
                state = state,
                onIntent = onIntent,
            )
        }
    }
}

@Composable
private fun SearchProfessionalScreenContent(
    modifier: Modifier = Modifier,
    state: SearchProfessionalState,
    onIntent: (SearchProfessionalIntent) -> Unit,
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
                onValueChange = { onIntent(SearchProfessionalIntent.EmailChanged(it)) },
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
                            onIntent(SearchProfessionalIntent.SearchClicked)
                            focusManager.clearFocus()
                        }
                    }
                )
            )

            Spacer(modifier = Modifier.height(DimensResources.spacing8))

            CustomOutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.phone,
                onValueChange = { onIntent(SearchProfessionalIntent.PhoneChanged(it)) },
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
                            onIntent(SearchProfessionalIntent.SearchClicked)
                            focusManager.clearFocus()
                        }
                    }
                )
            )

            Spacer(modifier = Modifier.height(DimensResources.spacing8))

            CustomOutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.collegiateNumber,
                onValueChange = { onIntent(SearchProfessionalIntent.CollegiateNumberChanged(it)) },
                label = stringResource(TokensResources.collegiateNumber),
                enabled = state.isCollegiateNumberEnabled,
                isError = state.collegiateNumberError != null && state.collegiateNumberError != ValidationError.FIELD_EMPTY,
                supportingText = state.collegiateNumberError?.takeIf { it != ValidationError.FIELD_EMPTY }
                    ?.let { error ->
                        { Text(text = error.asString()) }
                    },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        if (state.canSearch && !state.isLoading) {
                            onIntent(SearchProfessionalIntent.SearchClicked)
                            focusManager.clearFocus()
                        }
                    }
                )
            )

            Spacer(modifier = Modifier.height(DimensResources.spacing24))

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(space = DimensResources.spacing8),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    modifier = Modifier.padding(vertical = DimensResources.spacing4),
                    checked = state.isCheckAllProfessionals,
                    onCheckedChange = { onIntent(SearchProfessionalIntent.CheckAllProfessionalsClicked) },
                )

                Text(text = stringResource(TokensResources.checkAllProfessionals))
            }


            Button(
                onClick = { onIntent(SearchProfessionalIntent.SearchClicked) },
                enabled = state.canSearch && !state.isLoading &&
                        (state.isCheckAllProfessionals || (
                                (state.emailError == null || state.emailError == ValidationError.FIELD_EMPTY) &&
                                        (state.phoneError == null || state.phoneError == ValidationError.FIELD_EMPTY) &&
                                        (state.collegiateNumberError == null || state.collegiateNumberError == ValidationError.FIELD_EMPTY)
                                )),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(TokensResources.searchProfessional))
            }

            Spacer(modifier = Modifier.height(DimensResources.spacing40))

            state.error?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }
        }

        if (state.results.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(DimensResources.spacing12))

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = DimensResources.spacing8),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = { onIntent(SearchProfessionalIntent.ClearSearch) },
                    ) {
                        Text(
                            stringResource(TokensResources.clearSearches),
                            textAlign = TextAlign.End
                        )

                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = stringResource(TokensResources.clearAction),
                        )
                    }

                }

            }
        }

        items(state.results) { professional ->

            ProfessionalItem(
                professional = professional,
                onClick = { onIntent(SearchProfessionalIntent.ProfessionalSelected(professional)) }
            )
        }

        item {
            state.selectedProfessional?.let { professional ->
                Spacer(modifier = Modifier.height(DimensResources.spacing32))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(DimensResources.spacing16))
                ProfessionalDetails(professional = professional)
            }
        }
    }
}

@Composable
fun ProfessionalDetails(professional: ProfessionalUi) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(DimensResources.spacing8)
    ) {
        Text(
            text = stringResource(TokensResources.professionalDetails),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = DimensResources.spacing16)
        )

        DetailItem(label = stringResource(TokensResources.firstName), value = professional.firstName)
        DetailItem(label = stringResource(TokensResources.lastName), value = professional.lastName)
        DetailItem(label = stringResource(TokensResources.email), value = professional.email)
        DetailItem(label = stringResource(TokensResources.phone), value = professional.phone)
        DetailItem(label = stringResource(TokensResources.specialty), value = professional.specialty)
        DetailItem(label = stringResource(TokensResources.additionalNotes), value = professional.additionalInfo)
    }
}

@Composable
fun DetailItem(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = DimensResources.spacing4)) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
