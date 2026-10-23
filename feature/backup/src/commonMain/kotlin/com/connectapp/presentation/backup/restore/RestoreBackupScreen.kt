package com.connectapp.presentation.backup.restore

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.connectapp.commonresources.backup_file_selected
import com.connectapp.commonresources.backup_restore_confirm_text
import com.connectapp.commonresources.backup_restore_confirm_title
import com.connectapp.commonresources.backup_restore_intro_login
import com.connectapp.commonresources.backup_restore_intro_settings
import com.connectapp.commonresources.button_cancel
import com.connectapp.commonresources.button_choose_backup_file
import com.connectapp.commonresources.button_replace_data
import com.connectapp.commonresources.button_restore_backup
import com.connectapp.commonresources.component.ErrorMessage
import com.connectapp.commonresources.component.LoadingIndicator
import com.connectapp.commonresources.component.ValidatedTextField
import com.connectapp.commonresources.export.rememberDocumentPicker
import com.connectapp.commonresources.label_backup_password
import com.connectapp.commonresources.label_your_password
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * [fromSignIn]: opened from the sign-in screen of a tablet without accounts, so no session
 * password is asked and the restore is refused if the tablet has accounts after all.
 */
@Composable
fun RestoreBackupScreen(
    fromSignIn: Boolean,
    onRestored: () -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: RestoreBackupViewModel = koinViewModel { parametersOf(fromSignIn) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val picker = rememberDocumentPicker { picked ->
        if (picked != null) viewModel.onIntent(RestoreBackupIntent.FilePicked(picked.fileName, picked.bytes))
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                RestoreBackupEffect.Restored -> onRestored()
                RestoreBackupEffect.NavigateToLogin -> onNavigateToLogin()
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
        ) {
            RestoreBackupForm(state = state, onChooseFile = picker::pick, onIntent = viewModel::onIntent)
        }
    }

    if (state.showConfirmation) {
        AlertDialog(
            onDismissRequest = { viewModel.onIntent(RestoreBackupIntent.ConfirmationDismissed) },
            title = { Text(stringResource(backup_restore_confirm_title)) },
            text = { Text(stringResource(backup_restore_confirm_text)) },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.onIntent(RestoreBackupIntent.RestoreConfirmed) },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) {
                    Text(stringResource(button_replace_data))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onIntent(RestoreBackupIntent.ConfirmationDismissed) }) {
                    Text(stringResource(button_cancel))
                }
            },
        )
    }
}

@Composable
private fun ColumnScope.RestoreBackupForm(
    state: RestoreBackupState,
    onChooseFile: () -> Unit,
    onIntent: (RestoreBackupIntent) -> Unit,
) {
    Text(
        text = stringResource(if (state.fromSignIn) backup_restore_intro_login else backup_restore_intro_settings),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    OutlinedButton(
        onClick = onChooseFile,
        enabled = !state.isRestoring,
        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
        shape = MaterialTheme.shapes.large,
    ) {
        Icon(Icons.Default.FileOpen, contentDescription = null)
        Text(stringResource(button_choose_backup_file), modifier = Modifier.padding(start = 8.dp))
    }
    state.fileName?.let {
        Text(stringResource(backup_file_selected, it), style = MaterialTheme.typography.bodyLarge)
    }
    ValidatedTextField(
        value = state.password,
        onValueChange = { onIntent(RestoreBackupIntent.PasswordChanged(it)) },
        label = label_backup_password,
        error = null,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = if (state.fromSignIn) ImeAction.Done else ImeAction.Next,
        ),
        modifier = Modifier.fillMaxWidth(),
    )
    if (!state.fromSignIn) {
        ValidatedTextField(
            value = state.sessionPassword,
            onValueChange = { onIntent(RestoreBackupIntent.SessionPasswordChanged(it)) },
            label = label_your_password,
            error = null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth(),
        )
    }
    if (state.isRestoring) {
        LoadingIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
    } else {
        Button(
            onClick = { onIntent(RestoreBackupIntent.RestoreClicked) },
            enabled = state.canSubmit,
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            shape = MaterialTheme.shapes.large,
        ) {
            Text(stringResource(button_restore_backup), style = MaterialTheme.typography.titleMedium)
        }
    }
    state.errorMessage?.let { ErrorMessage(stringResource(it)) }
}
