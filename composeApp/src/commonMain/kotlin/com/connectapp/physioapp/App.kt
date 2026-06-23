package com.connectapp.physioapp

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import com.connectapp.presentation.ui.theme.PhysioTheme
import com.connectapp.presentation.MainScreen

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun App(windowSizeClass: WindowSizeClass) {
    PhysioTheme {
        MainScreen(windowSizeClass = windowSizeClass)
    }
}
