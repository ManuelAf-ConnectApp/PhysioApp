package com.connectapp.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.connectapp.designresources.DimensResources
import com.connectapp.designresources.TokensResources
import com.connectapp.presentation.component.AboutDialog
import com.connectapp.presentation.settings.component.DatabaseInfoDialog
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    snackBarHostState: SnackbarHostState,
    onLogoutSuccess: () -> Unit,
) {
    val viewModel: SettingsViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val onIntent = viewModel::onIntent

    val filePickerLauncher = rememberFilePickerLauncher { path ->
        path?.let { onIntent(SettingsIntent.SelectFilePath(it)) }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is SettingsEffect.ShowSnackbar -> {
                    snackBarHostState.showSnackbar(effect.message)
                }

                SettingsEffect.LogoutSuccess -> {
                    onLogoutSuccess()
                }
            }
        }
    }
    if (state.isLoading) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Box(modifier = modifier.fillMaxSize()) {
            SettingsScreenContent(
                modifier = Modifier,
                state = state,
                onIntent = onIntent,
                onSelectFile = filePickerLauncher
            )
        }
    }
}

@Composable
private fun SettingsScreenContent(
    modifier: Modifier = Modifier,
    state: SettingsState,
    onIntent: (SettingsIntent) -> Unit,
    onSelectFile: () -> Unit,
) {
    val appDatabaseName = stringResource(TokensResources.appDatabaseName)

    if (state.showImportInfoDialog) {
        DatabaseInfoDialog(
            onDismissRequest = { onIntent(SettingsIntent.DismissImportInfoDialog) }
        )
    }

    if (state.showAboutDialog) {
        AboutDialog(
            onDismissRequest = { onIntent(SettingsIntent.DismissAboutDialog) }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(all = DimensResources.spacing16),
        verticalArrangement = Arrangement.spacedBy(DimensResources.spacing16)
    ) {
        Text(
            text = stringResource(TokensResources.systemSettings),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier.padding(DimensResources.spacing16),
                verticalArrangement = Arrangement.spacedBy(DimensResources.spacing12)
            ) {
                Text(
                    text = stringResource(TokensResources.database),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = stringResource(TokensResources.databaseManagementDesc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Button(
                    onClick = { onIntent(SettingsIntent.ExportDatabase(databaseName = appDatabaseName)) },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(DimensResources.spacing12)
                ) {
                    Icon(Icons.Default.FileUpload, contentDescription = null)
                    Spacer(Modifier.width(DimensResources.spacing8))
                    Text(stringResource(TokensResources.exportDatabase))
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = DimensResources.spacing8))

                Text(
                    text = stringResource(TokensResources.importDatabase),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = stringResource(TokensResources.howToImportDatabase),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable {
                        onIntent(SettingsIntent.DismissImportInfoDialog)
                    }
                )

                OutlinedTextField(
                    value = state.selectedFilePath,
                    onValueChange = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectFile() },
                    label = { Text(stringResource(TokensResources.databaseToImport)) },
                    readOnly = true,
                    enabled = false, // Para que el click lo maneje el modifier.clickable
                    trailingIcon = {
                        IconButton(onClick = onSelectFile) {
                            Icon(
                                Icons.Default.FolderOpen,
                                contentDescription = stringResource(TokensResources.menu) // Or something appropriate
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                OutlinedButton(
                    onClick = { onIntent(SettingsIntent.ImportDatabase(databaseName = appDatabaseName)) },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(DimensResources.spacing12),
                    enabled = state.selectedFilePath.isNotBlank()
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null)
                    Spacer(Modifier.width(DimensResources.spacing8))
                    Text(stringResource(TokensResources.importDatabase))
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier.padding(DimensResources.spacing16),
                verticalArrangement = Arrangement.spacedBy(DimensResources.spacing12)
            ) {
                Text(
                    text = "Información", // This was hardcoded but I'll leave it or add it if needed. Wait, I see "Personal Info" in strings.
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedButton(
                    onClick = { onIntent(SettingsIntent.ShowAboutDialog) },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(DimensResources.spacing12)
                ) {
                    Icon(Icons.Default.Info, contentDescription = null)
                    Spacer(Modifier.width(DimensResources.spacing8))
                    Text(stringResource(TokensResources.about))
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { onIntent(SettingsIntent.Logout) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            ),
            contentPadding = PaddingValues(DimensResources.spacing12)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.Logout,
                contentDescription = stringResource(TokensResources.logout)
            )
            Spacer(Modifier.width(DimensResources.spacing8))
            Text(stringResource(TokensResources.logout))
        }
    }
}

