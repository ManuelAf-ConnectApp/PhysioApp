package com.connectapp.presentation.settings.recoverycode

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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.connectapp.commonresources.button_back_to_settings
import com.connectapp.commonresources.button_generate_code
import com.connectapp.commonresources.component.ErrorMessage
import com.connectapp.commonresources.component.LoadingIndicator
import com.connectapp.commonresources.component.RecoveryCodePanel
import com.connectapp.commonresources.component.ValidatedTextField
import com.connectapp.commonresources.label_password
import com.connectapp.commonresources.regenerate_code_instruction
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RegenerateRecoveryCodeScreen(
    onDone: () -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: RegenerateRecoveryCodeViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                RegenerateRecoveryCodeEffect.Done -> onDone()
                RegenerateRecoveryCodeEffect.NavigateToLogin -> onNavigateToLogin()
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
            val code = state.newRecoveryCode
            if (code == null) {
                PasswordStep(state = state, onIntent = viewModel::onIntent)
            } else {
                RecoveryCodePanel(
                    code = code,
                    confirmed = state.newRecoveryCodeConfirmed,
                    onConfirmedChange = {
                        viewModel.onIntent(RegenerateRecoveryCodeIntent.NewRecoveryCodeConfirmed(it))
                    },
                    continueLabel = button_back_to_settings,
                    onContinue = { viewModel.onIntent(RegenerateRecoveryCodeIntent.DoneClicked) },
                )
            }
        }
    }
}

@Composable
private fun PasswordStep(state: RegenerateRecoveryCodeState, onIntent: (RegenerateRecoveryCodeIntent) -> Unit) {
    Text(
        text = stringResource(regenerate_code_instruction),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    ValidatedTextField(
        value = state.password,
        onValueChange = { onIntent(RegenerateRecoveryCodeIntent.PasswordChanged(it)) },
        label = label_password,
        error = null,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
        modifier = Modifier.fillMaxWidth(),
    )
    if (state.isGenerating) {
        LoadingIndicator()
    } else {
        Button(
            onClick = { onIntent(RegenerateRecoveryCodeIntent.GenerateClicked) },
            enabled = state.canSubmit,
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            shape = MaterialTheme.shapes.large,
        ) {
            Text(stringResource(button_generate_code), style = MaterialTheme.typography.titleMedium)
        }
    }
    state.errorMessage?.let {
        ErrorMessage(stringResource(it))
    }
}
