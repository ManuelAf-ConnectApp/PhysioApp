package com.connectapp.commonresources.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.connectapp.commonresources.button_cancel
import com.connectapp.commonresources.value_not_provided
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** One labelled value of a record (professional, patient…); a missing optional value says so. */
@Composable
fun RecordField(label: StringResource, value: String?, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = stringResource(label),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value ?: stringResource(value_not_provided),
            style = MaterialTheme.typography.bodyLarge,
            color = with(MaterialTheme.colorScheme) { if (value == null) onSurfaceVariant else onSurface },
        )
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
    }
}

/** Confirmation of a destructive or hard-to-undo action (archive, delete…), in the error colour. */
@Composable
fun ConfirmationDialog(
    title: StringResource,
    message: StringResource,
    confirm: StringResource,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(title)) },
        text = { Text(stringResource(message)) },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
            ) {
                Text(stringResource(confirm))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(button_cancel)) } },
    )
}
