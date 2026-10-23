package com.connectapp.presentation.forgot_password

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
import com.connectapp.commonresources.button_back_to_login
import com.connectapp.commonresources.button_reset_password
import com.connectapp.commonresources.component.ErrorMessage
import com.connectapp.commonresources.component.LoadingIndicator
import com.connectapp.commonresources.component.RecoveryCodePanel
import com.connectapp.commonresources.component.ValidatedTextField
import com.connectapp.commonresources.error_invalid_email
import com.connectapp.commonresources.error_password_policy
import com.connectapp.commonresources.error_passwords_not_match
import com.connectapp.commonresources.error_recovery_code_format
import com.connectapp.commonresources.forgot_password_instruction
import com.connectapp.commonresources.forgot_password_success
import com.connectapp.commonresources.label_confirm_new_password
import com.connectapp.commonresources.label_email
import com.connectapp.commonresources.label_new_password
import com.connectapp.commonresources.label_recovery_code
import com.connectapp.commonresources.password_policy_hint
import com.connectapp.domain.validation.AuthField
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ForgotPasswordScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
) {
    val viewModel: ForgotPasswordViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                ForgotPasswordEffect.NavigateToLogin -> onNavigateBack()
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
            val newCode = state.newRecoveryCode
            if (newCode == null) {
                ResetForm(state = state, onIntent = viewModel::onIntent)
            } else {
                Text(
                    text = stringResource(forgot_password_success),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(24.dp))
                RecoveryCodePanel(
                    code = newCode,
                    confirmed = state.newRecoveryCodeConfirmed,
                    onConfirmedChange = { viewModel.onIntent(ForgotPasswordIntent.NewRecoveryCodeConfirmed(it)) },
                    continueLabel = button_back_to_login,
                    onContinue = { viewModel.onIntent(ForgotPasswordIntent.BackToLoginClicked) },
                )
            }
        }
    }
}

@Composable
private fun ResetForm(state: ForgotPasswordState, onIntent: (ForgotPasswordIntent) -> Unit) {
    val invalid = state.invalidFields
    val fieldModifier = Modifier.fillMaxWidth()

    Text(
        text = stringResource(forgot_password_instruction),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(modifier = Modifier.height(24.dp))

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ValidatedTextField(
            value = state.email,
            onValueChange = { onIntent(ForgotPasswordIntent.EmailChanged(it)) },
            label = label_email,
            error = error_invalid_email.takeIf { AuthField.EMAIL in invalid },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = fieldModifier,
        )
        ValidatedTextField(
            value = state.recoveryCode,
            onValueChange = { onIntent(ForgotPasswordIntent.RecoveryCodeChanged(it)) },
            label = label_recovery_code,
            error = error_recovery_code_format.takeIf { AuthField.RECOVERY_CODE in invalid },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Ascii,
                capitalization = KeyboardCapitalization.Characters,
            ),
            modifier = fieldModifier,
        )
        ValidatedTextField(
            value = state.newPassword,
            onValueChange = { onIntent(ForgotPasswordIntent.NewPasswordChanged(it)) },
            label = label_new_password,
            error = error_password_policy.takeIf { AuthField.PASSWORD in invalid },
            supportingText = password_policy_hint,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = fieldModifier,
        )
        ValidatedTextField(
            value = state.confirmPassword,
            onValueChange = { onIntent(ForgotPasswordIntent.ConfirmPasswordChanged(it)) },
            label = label_confirm_new_password,
            error = error_passwords_not_match.takeIf { state.confirmPassword != state.newPassword },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            modifier = fieldModifier,
        )
    }

    Spacer(modifier = Modifier.height(24.dp))

    if (state.isLoading) {
        LoadingIndicator()
    } else {
        Button(
            onClick = { onIntent(ForgotPasswordIntent.ResetClicked) },
            enabled = state.canSubmit,
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            shape = MaterialTheme.shapes.large,
        ) {
            Text(stringResource(button_reset_password), style = MaterialTheme.typography.titleMedium)
        }
    }

    state.errorMessage?.let {
        Spacer(modifier = Modifier.height(16.dp))
        ErrorMessage(stringResource(it))
    }

    TextButton(onClick = { onIntent(ForgotPasswordIntent.BackToLoginClicked) }) {
        Text(stringResource(button_back_to_login))
    }
}
