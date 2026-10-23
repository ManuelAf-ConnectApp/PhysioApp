package com.connectapp.presentation.home.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.connectapp.commonresources.button_make_backup
import com.connectapp.commonresources.format.formatDateTime
import com.connectapp.commonresources.home_backup_reminder_never
import com.connectapp.commonresources.home_backup_reminder_old
import org.jetbrains.compose.resources.stringResource

/**
 * Home's reminder to make a backup (plan §21.7): shown when there has never been one on this
 * tablet or the last one is a week old. It does not block anything.
 */
@Composable
internal fun BackupReminderCard(lastBackupAt: Long?, onMakeBackup: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        ),
        shape = MaterialTheme.shapes.large,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.Backup, contentDescription = null)
            Text(
                text = if (lastBackupAt == null) {
                    stringResource(home_backup_reminder_never)
                } else {
                    stringResource(home_backup_reminder_old, formatDateTime(lastBackupAt))
                },
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
            )
            FilledTonalButton(onClick = onMakeBackup, modifier = Modifier.heightIn(min = 48.dp)) {
                Text(stringResource(button_make_backup))
            }
        }
    }
}
