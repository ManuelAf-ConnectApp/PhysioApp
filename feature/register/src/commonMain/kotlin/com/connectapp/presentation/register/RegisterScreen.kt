package com.connectapp.presentation.register

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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.connectapp.commonresources.button_go_to_login
import com.connectapp.commonresources.button_register
import com.connectapp.commonresources.component.ErrorMessage
import com.connectapp.commonresources.component.LoadingIndicator
import com.connectapp.commonresources.component.RecoveryCodePanel
import com.connectapp.commonresources.component.ValidatedTextField
import com.connectapp.commonresources.error_invalid_email
import com.connectapp.commonresources.error_password_policy
import com.connectapp.commonresources.error_passwords_not_match
import com.connectapp.commonresources.error_required_field
import com.connectapp.commonresources.label_confirm_password
import com.connectapp.commonresources.label_email
import com.connectapp.commonresources.label_first_name
import com.connectapp.commonresources.label_last_name
import com.connectapp.commonresources.label_password
import com.connectapp.commonresources.password_policy_hint
import com.connectapp.commonresources.register_already_account
import com.connectapp.commonresources.register_success_next_step
import com.connectapp.commonresources.register_success_title
import com.connectapp.domain.validation.AuthField
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RegisterScreen(
    modifier: Modifier = Modifier,
    onNavigateToLogin: () -> Unit,
) {
    val viewModel: RegisterViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                RegisterEffect.NavigateToLogin -> onNavigateToLogin()
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
        Column(modifier = Modifier.widthIn(max = 560.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            val code = state.recoveryCode
            if (code == null) {
                RegisterForm(state = state, onIntent = viewModel::onIntent)
            } else {
                Text(
                    text = stringResource(register_success_title),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(register_success_next_step), style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(24.dp))
                RecoveryCodePanel(
                    code = code,
                    confirmed = state.recoveryCodeConfirmed,
                    onConfirmedChange = { viewModel.onIntent(RegisterIntent.RecoveryCodeConfirmed(it)) },
                    continueLabel = button_go_to_login,
                    onContinue = { viewModel.onIntent(RegisterIntent.ContinueClicked) },
                )
            }
        }
    }
}

@Composable
private fun RegisterForm(state: RegisterState, onIntent: (RegisterIntent) -> Unit) {
    val invalid = state.invalidFields
    val fieldModifier = Modifier.fillMaxWidth()

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ValidatedTextField(
            value = state.firstName,
            onValueChange = { onIntent(RegisterIntent.FirstNameChanged(it)) },
            label = label_first_name,
            error = error_required_field.takeIf { AuthField.FIRST_NAME in invalid },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
            modifier = fieldModifier,
        )
        ValidatedTextField(
            value = state.lastName,
            onValueChange = { onIntent(RegisterIntent.LastNameChanged(it)) },
            label = label_last_name,
            error = error_required_field.takeIf { AuthField.LAST_NAME in invalid },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
            modifier = fieldModifier,
        )
        ValidatedTextField(
            value = state.email,
            onValueChange = { onIntent(RegisterIntent.EmailChanged(it)) },
            label = label_email,
            error = error_invalid_email.takeIf { AuthField.EMAIL in invalid },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = fieldModifier,
        )
        ValidatedTextField(
            value = state.password,
            onValueChange = { onIntent(RegisterIntent.PasswordChanged(it)) },
            label = label_password,
            error = error_password_policy.takeIf { AuthField.PASSWORD in invalid },
            supportingText = password_policy_hint,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = fieldModifier,
        )
        ValidatedTextField(
            value = state.confirmPassword,
            onValueChange = { onIntent(RegisterIntent.ConfirmPasswordChanged(it)) },
            label = label_confirm_password,
            // The password's own error covers a policy breach; here only a mismatch is reported.
            error = error_passwords_not_match.takeIf { state.confirmPassword != state.password },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            modifier = fieldModifier,
        )
    }

    Spacer(modifier = Modifier.height(24.dp))

    if (state.isLoading) {
        LoadingIndicator()
    } else {
        Button(
            onClick = { onIntent(RegisterIntent.RegisterClicked) },
            enabled = state.canSubmit,
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            shape = MaterialTheme.shapes.large,
        ) {
            Text(stringResource(button_register), style = MaterialTheme.typography.titleMedium)
        }
    }

    state.errorMessage?.let {
        Spacer(modifier = Modifier.height(16.dp))
        ErrorMessage(stringResource(it))
    }

    TextButton(onClick = { onIntent(RegisterIntent.LoginClicked) }) {
        Text(stringResource(register_already_account))
    }
}
