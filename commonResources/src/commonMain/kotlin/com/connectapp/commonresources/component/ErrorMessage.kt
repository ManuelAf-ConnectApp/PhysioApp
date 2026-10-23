package com.connectapp.commonresources.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics

/**
 * The error left by a failed action (wrong password, duplicate record…). Screen readers announce
 * it as soon as it appears, since it shows up away from where the user is focused.
 */
@Composable
fun ErrorMessage(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodyMedium,
        modifier = modifier.semantics { liveRegion = LiveRegionMode.Assertive },
    )
}
