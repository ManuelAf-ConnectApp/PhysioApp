package com.connectapp.presentation.onboarding

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.connectapp.commonresources.button_save
import com.connectapp.commonresources.component.ErrorMessage
import com.connectapp.commonresources.component.LoadingIndicator
import com.connectapp.commonresources.component.SelectField
import com.connectapp.commonresources.component.ValidatedTextField
import com.connectapp.commonresources.error_invalid_license_number
import com.connectapp.commonresources.error_invalid_nif
import com.connectapp.commonresources.error_required_field
import com.connectapp.commonresources.error_select_college
import com.connectapp.commonresources.label_college
import com.connectapp.commonresources.label_first_name
import com.connectapp.commonresources.label_last_name
import com.connectapp.commonresources.label_license_number
import com.connectapp.commonresources.label_nif
import com.connectapp.commonresources.license_number_hint
import com.connectapp.commonresources.logout
import com.connectapp.commonresources.onboarding_subtitle
import com.connectapp.commonresources.onboarding_title
import com.connectapp.domain.shared.professional.ProfessionalCollege
import com.connectapp.domain.validation.AuthField
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun OnboardingScreen(
    modifier: Modifier = Modifier,
    onNavigateToHome: () -> Unit,
    onNavigateToLogin: () -> Unit,
) {
    val viewModel: OnboardingViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val onIntent = viewModel::onIntent

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                OnboardingEffect.NavigateToHome -> onNavigateToHome()
                OnboardingEffect.NavigateToLogin -> onNavigateToLogin()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Column(
            modifier = Modifier.widthIn(max = 560.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OnboardingForm(state = state, onIntent = onIntent)
        }
    }
}

@Composable
private fun OnboardingForm(state: OnboardingState, onIntent: (OnboardingIntent) -> Unit) {
    val invalid = state.invalidFields
    val fieldModifier = Modifier.fillMaxWidth()

    Text(
        text = stringResource(onboarding_title),
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.primary,
    )
    Text(
        text = stringResource(onboarding_subtitle),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(modifier = Modifier.height(16.dp))

    ValidatedTextField(
        value = state.nif,
        onValueChange = { onIntent(OnboardingIntent.NifChanged(it)) },
        label = label_nif,
        error = error_invalid_nif.takeIf { AuthField.NIF in invalid },
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
        modifier = fieldModifier,
    )
    SelectField(
        selected = state.college?.displayName,
        options = ProfessionalCollege.entries.map { it.displayName },
        onSelected = { onIntent(OnboardingIntent.CollegeSelected(ProfessionalCollege.entries[it])) },
        label = label_college,
        error = error_select_college.takeIf { AuthField.COLLEGE in invalid },
        modifier = fieldModifier,
    )
    ValidatedTextField(
        value = state.licenseNumber,
        onValueChange = { onIntent(OnboardingIntent.LicenseNumberChanged(it)) },
        label = label_license_number,
        error = error_invalid_license_number.takeIf { AuthField.LICENSE_NUMBER in invalid },
        supportingText = license_number_hint,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
        modifier = fieldModifier,
    )
    ValidatedTextField(
        value = state.firstName,
        onValueChange = { onIntent(OnboardingIntent.FirstNameChanged(it)) },
        label = label_first_name,
        error = error_required_field.takeIf { AuthField.FIRST_NAME in invalid },
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
        modifier = fieldModifier,
    )
    ValidatedTextField(
        value = state.lastName,
        onValueChange = { onIntent(OnboardingIntent.LastNameChanged(it)) },
        label = label_last_name,
        error = error_required_field.takeIf { AuthField.LAST_NAME in invalid },
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
        modifier = fieldModifier,
    )

    Spacer(modifier = Modifier.height(16.dp))

    if (state.isLoading) {
        LoadingIndicator()
    } else {
        Button(
            onClick = { onIntent(OnboardingIntent.SaveClicked) },
            enabled = state.canSubmit,
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            shape = MaterialTheme.shapes.large,
        ) {
            Text(stringResource(button_save), style = MaterialTheme.typography.titleMedium)
        }
    }

    state.errorMessage?.let {
        ErrorMessage(stringResource(it))
    }

    TextButton(onClick = { onIntent(OnboardingIntent.LogoutClicked) }) {
        Text(stringResource(logout))
    }
}
