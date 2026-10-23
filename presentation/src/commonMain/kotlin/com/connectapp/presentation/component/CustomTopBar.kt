package com.connectapp.presentation.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.connectapp.commonresources.back
import org.jetbrains.compose.resources.stringResource

/**
 * The top bar shows **only** the screen title — no actions unless explicitly
 * agreed. A back arrow is shown only when [onBack] is given (register and forgot password,
 * agreed on 2026-10-19); elsewhere going back relies on the system back navigation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTopBar(title: String, onBack: (() -> Unit)? = null) {
    CenterAlignedTopAppBar(
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(back))
                }
            }
        },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.semantics { heading() },
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            scrolledContainerColor = Color.Unspecified,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    )
}
