package com.connectapp.presentation.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = PhysioPrimary,
    onPrimary = PhysioOnPrimary,
    primaryContainer = PhysioSecondary,
    secondary = PhysioTertiary,
    background = PhysioBackground,
    surface = PhysioSurface,
    onBackground = PhysioOnBackground,
    outline = PhysioOutline,
    error = PhysioError
)

private val DarkColorScheme = darkColorScheme(
    primary = PhysioSecondary,
    onPrimary = PhysioOnBackground,
    background = PhysioOnBackground,
    surface = PhysioOnBackground,
    onBackground = PhysioBackground
)

@Composable
fun PhysioTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
