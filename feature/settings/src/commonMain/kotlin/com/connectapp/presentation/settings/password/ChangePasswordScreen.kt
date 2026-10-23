package com.connectapp.presentation.settings.password

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.connectapp.commonresources.button_back_to_settings
import com.connectapp.commonresources.change_password_instruction
import com.connectapp.commonresources.change_password_success
import com.connectapp.commonresources.component.ErrorMessage
import com.connectapp.commonresources.component.LoadingIndicator
import com.connectapp.commonresources.component.ValidatedTextField
import com.connectapp.commonresources.error_password_policy
import com.connectapp.commonresources.error_passwords_not_match
import com.connectapp.commonresources.label_confirm_new_password
import com.connectapp.commonresources.label_current_password
import com.connectapp.commonresources.label_new_password
import com.connectapp.commonresources.navigation.GuardUnsavedChanges
import com.connectapp.commonresources.password_policy_hint
import com.connectapp.commonresources.settings_item_change_password
import com.connectapp.domain.validation.AuthField
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ChangePasswordScreen(
    onDone: () -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: ChangePasswordViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    GuardUnsavedChanges(dirty = state.hasUnsavedChanges)

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                ChangePasswordEffect.NavigateToLogin -> onNavigateToLogin()
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
    ) {
        Column(
            modifier = Modifier.widthIn(max = 560.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (state.isDone) {
                Text(
                    text = stringResource(change_password_success),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                )
                Button(onClick = onDone, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
                    Text(stringResource(button_back_to_settings), style = MaterialTheme.typography.titleMedium)
                }
            } else {
                ChangePasswordForm(state = state, onIntent = viewModel::onIntent)
            }
        }
    }
}

@Composable
private fun ChangePasswordForm(state: ChangePasswordState, onIntent: (ChangePasswordIntent) -> Unit) {
    val invalid = state.invalidFields
    val fieldModifier = Modifier.fillMaxWidth()

    Text(
        text = stringResource(change_password_instruction),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ValidatedTextField(
            value = state.currentPassword,
            onValueChange = { onIntent(ChangePasswordIntent.CurrentPasswordChanged(it)) },
            label = label_current_password,
            error = null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
            modifier = fieldModifier,
        )
        ValidatedTextField(
            value = state.newPassword,
            onValueChange = { onIntent(ChangePasswordIntent.NewPasswordChanged(it)) },
            label = label_new_password,
            error = error_password_policy.takeIf { AuthField.PASSWORD in invalid },
            supportingText = password_policy_hint,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
            modifier = fieldModifier,
        )
        ValidatedTextField(
            value = state.confirmPassword,
            onValueChange = { onIntent(ChangePasswordIntent.ConfirmPasswordChanged(it)) },
            label = label_confirm_new_password,
            error = error_passwords_not_match.takeIf { AuthField.PASSWORD_CONFIRMATION in invalid },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            modifier = fieldModifier,
        )
    }

    if (state.isSaving) {
        LoadingIndicator()
    } else {
        Button(
            onClick = { onIntent(ChangePasswordIntent.SaveClicked) },
            enabled = state.canSubmit,
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            shape = MaterialTheme.shapes.large,
        ) {
            Text(stringResource(settings_item_change_password), style = MaterialTheme.typography.titleMedium)
        }
    }

    state.errorMessage?.let {
        ErrorMessage(stringResource(it))
    }
}
