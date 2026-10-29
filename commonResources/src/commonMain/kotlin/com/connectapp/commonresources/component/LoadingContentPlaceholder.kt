package com.connectapp.commonresources.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.connectapp.commonresources.content_desc_loading
import org.jetbrains.compose.resources.stringResource

/**
 * Static content skeleton used while a screen reads its initial data.
 *
 * Unlike a full-screen spinner, it keeps the new screen visually present from its first frame and
 * avoids suggesting that navigation itself is slow. Long-running actions still use
 * [LoadingIndicator], which communicates that interaction is temporarily blocked.
 */
@Composable
fun LoadingContentPlaceholder(modifier: Modifier = Modifier) {
    val description = stringResource(content_desc_loading)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 720.dp)
            .padding(24.dp)
            .semantics { contentDescription = description },
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        PlaceholderBlock(height = 28.dp, widthFraction = 0.45f)
        PlaceholderBlock(height = 112.dp)
        PlaceholderBlock(height = 72.dp)
        PlaceholderBlock(height = 72.dp, widthFraction = 0.72f)
    }
}

@Composable
private fun PlaceholderBlock(height: androidx.compose.ui.unit.Dp, widthFraction: Float = 1f) {
    Surface(
        modifier = Modifier
            .fillMaxWidth(widthFraction)
            .height(height),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {}
}
