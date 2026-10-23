package com.connectapp.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.connectapp.commonresources.*
import com.connectapp.commonresources.format.formatDateTime
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onLogout: () -> Unit,
    onNavigate: (SettingsDestination) -> Unit,
) {
    val viewModel: SettingsViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                SettingsEffect.NavigateToLogin -> onLogout()
            }
        }
    }

    // Same reading width as Profile: rows stretched across a landscape tablet are hard to scan.
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier.widthIn(max = 720.dp).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                ProfileHeader(
                    name = state.userName,
                    email = state.userEmail,
                )
            }

            item { SectionHeader(title = stringResource(settings_section_security)) }
            item {
                SettingsGroup {
                    SettingsRow(
                        icon = Icons.Default.Password,
                        title = stringResource(settings_item_change_password),
                        subtitle = stringResource(settings_item_change_password_subtitle),
                        onClick = { onNavigate(SettingsDestination.CHANGE_PASSWORD) },
                    )
                    HorizontalDivider()
                    SettingsRow(
                        icon = Icons.Default.Key,
                        title = stringResource(settings_item_recovery_code),
                        subtitle = stringResource(settings_item_recovery_code_subtitle),
                        onClick = { onNavigate(SettingsDestination.RECOVERY_CODE) },
                    )
                }
            }

            item { SectionHeader(title = stringResource(settings_section_backup)) }
            item {
                val lastBackup = state.lastBackupAt
                SettingsGroup {
                    SettingsRow(
                        icon = Icons.Default.Backup,
                        title = stringResource(settings_item_backup_create),
                        subtitle = if (lastBackup == null) {
                            stringResource(settings_backup_never)
                        } else {
                            stringResource(settings_backup_last, formatDateTime(lastBackup))
                        },
                        onClick = { onNavigate(SettingsDestination.CREATE_BACKUP) },
                    )
                    HorizontalDivider()
                    SettingsRow(
                        icon = Icons.Default.Restore,
                        title = stringResource(settings_item_backup_restore),
                        subtitle = stringResource(settings_item_backup_restore_subtitle),
                        onClick = { onNavigate(SettingsDestination.RESTORE_BACKUP) },
                    )
                }
            }

            item { SectionHeader(title = stringResource(settings_section_more)) }
            item {
                SettingsGroup {
                    SettingsRow(
                        icon = Icons.Default.Info,
                        title = stringResource(settings_item_about),
                        subtitle = stringResource(settings_item_version, state.appVersion),
                    )
                    HorizontalDivider()
                    SettingsRow(
                        icon = Icons.Default.Gavel,
                        title = stringResource(settings_item_licenses),
                        subtitle = stringResource(settings_item_licenses_subtitle),
                        onClick = { onNavigate(SettingsDestination.LICENSES) },
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            item {
                Button(
                    onClick = { viewModel.onIntent(SettingsIntent.LogoutClicked) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp),
                    shape = MaterialTheme.shapes.large,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(logout))
                }
            }
        }
    }
}

@Composable
private fun ProfileHeader(
    name: String,
    email: String,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name.take(1).uppercase(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SettingsGroup(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp), content = content)
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .padding(top = 16.dp, bottom = 8.dp)
            .semantics { heading() }
    )
}

/**
 * A settings entry. With [onClick] it opens a screen (whole row as one button, with a chevron);
 * without it the row is purely informational (e.g. the app version).
 */
@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (onClick != null) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
