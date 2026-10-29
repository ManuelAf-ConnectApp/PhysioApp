package com.connectapp.presentation.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.connectapp.commonresources.*
import com.connectapp.commonresources.component.ErrorMessage
import com.connectapp.commonresources.component.LoadingIndicator
import com.connectapp.commonresources.component.PhysioLogo
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToOnboarding: () -> Unit,
    onNavigateToRestoreBackup: () -> Unit,
) {
    val viewModel: LoginViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val onIntent = viewModel::onIntent

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                LoginEffect.NavigateToHome -> onNavigateToHome()
                LoginEffect.NavigateToOnboarding -> onNavigateToOnboarding()
                LoginEffect.NavigateToRegister -> onNavigateToRegister()
                LoginEffect.NavigateToForgotPassword -> onNavigateToForgotPassword()
                LoginEffect.NavigateToRestoreBackup -> onNavigateToRestoreBackup()
            }
        }
    }

    if (state.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            LoadingIndicator()
        }
    } else {
        LoginContent(
            modifier = modifier,
            state = state,
            onIntent = onIntent,
        )
    }

}

@Composable
private fun LoginContent(
    modifier: Modifier = Modifier,
    state: LoginState,
    onIntent: (LoginIntent) -> Unit,
) {

    Box(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 520.dp)
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
        PhysioLogo(modifier = Modifier.size(112.dp))

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(login_title),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(login_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = state.email,
            onValueChange = { onIntent(LoginIntent.EmailChanged(it)) },
            label = { Text(stringResource(label_email)) },
            placeholder = { Text(stringResource(placeholder_email)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = state.password,
            onValueChange = { onIntent(LoginIntent.PasswordChanged(it)) },
            label = { Text(stringResource(label_password)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            visualTransformation = if (state.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                val image =
                    if (state.isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                IconButton(onClick = {
                    onIntent(LoginIntent.TogglePasswordVisibility)
                }) {
                    Icon(imageVector = image, contentDescription = stringResource(content_desc_toggle_password))
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                // One control for screen readers: the whole row toggles, the checkbox only shows it.
                .toggleable(
                    value = state.rememberSessionEnabled,
                    role = Role.Checkbox,
                    onValueChange = { onIntent(LoginIntent.RememberSessionToggled(it)) },
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = state.rememberSessionEnabled,
                onCheckedChange = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(login_remember_session),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterEnd
        ) {
            TextButton(onClick = { onIntent(LoginIntent.ForgotPasswordClicked) }) {
                Text(stringResource(login_forgot_password))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onIntent(LoginIntent.LoginClicked) },
            enabled = state.email.isNotBlank() && state.password.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Text(stringResource(button_login), style = MaterialTheme.typography.titleMedium)
        }

        state.errorMessage?.let {
            Spacer(modifier = Modifier.height(16.dp))
            ErrorMessage(state.errorArgument?.let { argument -> stringResource(it, argument) } ?: stringResource(it))
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(login_no_account))
            TextButton(onClick = { onIntent(LoginIntent.RegisterClicked) }) {
                Text(stringResource(login_register_now))
            }
        }

        if (state.canRestoreBackup) {
            TextButton(onClick = { onIntent(LoginIntent.RestoreBackupClicked) }) {
                Text(stringResource(login_restore_backup))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
