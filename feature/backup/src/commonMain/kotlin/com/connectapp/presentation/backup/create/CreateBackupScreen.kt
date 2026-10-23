package com.connectapp.presentation.backup.create

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
import com.connectapp.commonresources.backup_create_intro
import com.connectapp.commonresources.backup_create_password_warning
import com.connectapp.commonresources.backup_create_where
import com.connectapp.commonresources.backup_saved
import com.connectapp.commonresources.button_create_backup
import com.connectapp.commonresources.component.ErrorMessage
import com.connectapp.commonresources.component.LoadingIndicator
import com.connectapp.commonresources.component.ValidatedTextField
import com.connectapp.commonresources.error_password_policy
import com.connectapp.commonresources.error_passwords_not_match
import com.connectapp.commonresources.export.rememberDocumentExporter
import com.connectapp.commonresources.format.formatDateTime
import com.connectapp.commonresources.label_backup_password
import com.connectapp.commonresources.label_confirm_backup_password
import com.connectapp.commonresources.password_policy_hint
import com.connectapp.commonresources.settings_backup_last
import com.connectapp.commonresources.settings_backup_never
import com.connectapp.domain.backup.BackupField
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CreateBackupScreen(modifier: Modifier = Modifier) {
    val viewModel: CreateBackupViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val exporter = rememberDocumentExporter()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is CreateBackupEffect.SaveFile -> exporter.save(effect.document) { saved ->
                    viewModel.onIntent(CreateBackupIntent.SaveFinished(saved))
                }
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
            CreateBackupForm(state = state, onIntent = viewModel::onIntent)
        }
    }
}

@Composable
private fun ColumnScope.CreateBackupForm(state: CreateBackupState, onIntent: (CreateBackupIntent) -> Unit) {
    val lastBackup = state.lastBackupAt
    Text(
        text = if (lastBackup == null) {
            stringResource(settings_backup_never)
        } else {
            stringResource(settings_backup_last, formatDateTime(lastBackup))
        },
        style = MaterialTheme.typography.titleMedium,
    )
    listOf(backup_create_intro, backup_create_password_warning, backup_create_where).forEach {
        Text(
            text = stringResource(it),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    val invalid = state.invalidFields
    ValidatedTextField(
        value = state.password,
        onValueChange = { onIntent(CreateBackupIntent.PasswordChanged(it)) },
        label = label_backup_password,
        error = error_password_policy.takeIf { BackupField.PASSWORD in invalid },
        supportingText = password_policy_hint,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
        modifier = Modifier.fillMaxWidth(),
    )
    ValidatedTextField(
        value = state.confirmation,
        onValueChange = { onIntent(CreateBackupIntent.ConfirmationChanged(it)) },
        label = label_confirm_backup_password,
        error = error_passwords_not_match.takeIf { BackupField.PASSWORD_CONFIRMATION in invalid },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
        modifier = Modifier.fillMaxWidth(),
    )
    if (state.isCreating || state.isSaving) {
        LoadingIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
    } else {
        Button(
            onClick = { onIntent(CreateBackupIntent.CreateClicked) },
            enabled = state.canSubmit,
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            shape = MaterialTheme.shapes.large,
        ) {
            Text(stringResource(button_create_backup), style = MaterialTheme.typography.titleMedium)
        }
    }
    if (state.saved) {
        Text(
            text = stringResource(backup_saved),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
        )
    }
    state.errorMessage?.let { ErrorMessage(stringResource(it)) }
}
