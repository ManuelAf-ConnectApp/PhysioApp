package com.connectapp.presentation.settings

import androidx.compose.runtime.Composable

@Composable
expect fun rememberFilePickerLauncher(onResult: (String?) -> Unit): () -> Unit
