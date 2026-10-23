package com.connectapp.commonresources.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.connectapp.commonresources.recovery_code_confirm
import com.connectapp.commonresources.recovery_code_explanation
import com.connectapp.commonresources.recovery_code_title
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Shows a one-time recovery code (D4) and only lets the user continue after confirming that it
 * has been stored. The code is never copied to the clipboard, which other apps could read.
 */
@Composable
fun RecoveryCodePanel(
    code: String,
    confirmed: Boolean,
    onConfirmedChange: (Boolean) -> Unit,
    continueLabel: StringResource,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(recovery_code_title),
            style = MaterialTheme.typography.titleLarge,
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        ) {
            // Always on one line: a code split across lines is easy to copy down wrongly.
            BasicText(
                text = code,
                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp, horizontal = 16.dp),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                ),
                maxLines = 1,
                softWrap = false,
                autoSize = TextAutoSize.StepBased(minFontSize = 12.sp, maxFontSize = 28.sp),
            )
        }
        Text(
            text = stringResource(recovery_code_explanation),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .toggleable(value = confirmed, role = Role.Checkbox, onValueChange = onConfirmedChange),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(checked = confirmed, onCheckedChange = null)
            Text(text = stringResource(recovery_code_confirm), style = MaterialTheme.typography.bodyMedium)
        }
        Button(
            onClick = onContinue,
            enabled = confirmed,
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            shape = MaterialTheme.shapes.large,
        ) {
            Text(stringResource(continueLabel), style = MaterialTheme.typography.titleMedium)
        }
    }
}
