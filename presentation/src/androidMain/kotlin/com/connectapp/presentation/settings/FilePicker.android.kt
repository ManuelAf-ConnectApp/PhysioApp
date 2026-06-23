package com.connectapp.presentation.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable

@Composable
actual fun rememberFilePickerLauncher(onResult: (String?) -> Unit): () -> Unit {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            onResult(uri?.toString())
        }
    )

    return {
        // En Android podemos filtrar por tipo de archivo, para bases de datos SQLite 
        // a menudo se usa application/octet-stream o se deja genérico */*
        launcher.launch("*/*")
    }
}
