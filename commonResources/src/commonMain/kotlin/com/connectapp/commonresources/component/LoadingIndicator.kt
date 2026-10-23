package com.connectapp.commonresources.component

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.connectapp.commonresources.content_desc_loading
import org.jetbrains.compose.resources.stringResource

/** Indeterminate progress that screen readers announce as "Loading" instead of skipping it. */
@Composable
fun LoadingIndicator(modifier: Modifier = Modifier) {
    val description = stringResource(content_desc_loading)
    CircularProgressIndicator(modifier = modifier.semantics { contentDescription = description })
}
