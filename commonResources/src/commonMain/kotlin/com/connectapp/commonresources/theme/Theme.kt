package com.connectapp.commonresources.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = Primary40,
    onPrimary = Neutral99,
    primaryContainer = Primary90,
    onPrimaryContainer = Primary10,
    secondary = Secondary40,
    onSecondary = Neutral99,
    secondaryContainer = Secondary90,
    onSecondaryContainer = Secondary10,
    tertiary = Tertiary40,
    onTertiary = Neutral99,
    tertiaryContainer = Tertiary90,
    onTertiaryContainer = Tertiary10,
    error = Error40,
    onError = Neutral99,
    errorContainer = Error90,
    onErrorContainer = Error10,
    background = Neutral99,
    onBackground = Neutral10,
    surface = Neutral99,
    onSurface = Neutral10,
    surfaceVariant = Neutral90,
    onSurfaceVariant = Neutral30,
    outline = Neutral50,
    outlineVariant = Neutral80,
    inverseSurface = Neutral30,
    inverseOnSurface = Neutral90,
    inversePrimary = Primary80,
)

private val DarkColors = darkColorScheme(
    primary = Primary80,
    onPrimary = Primary20,
    primaryContainer = Primary30,
    onPrimaryContainer = Primary90,
    secondary = Secondary80,
    onSecondary = Secondary20,
    secondaryContainer = Secondary30,
    onSecondaryContainer = Secondary90,
    tertiary = Tertiary80,
    onTertiary = Tertiary20,
    tertiaryContainer = Tertiary30,
    onTertiaryContainer = Tertiary90,
    error = Error80,
    onError = Error10,
    errorContainer = ErrorContainerDark,
    onErrorContainer = Error90,
    background = NeutralDark10,
    onBackground = NeutralDark87,
    surface = NeutralDark10,
    onSurface = NeutralDark87,
    surfaceVariant = NeutralDark30,
    onSurfaceVariant = NeutralDarkSurfaceVariant,
    outline = NeutralDark50,
    outlineVariant = NeutralDark30,
    inverseSurface = NeutralDark87,
    inverseOnSurface = NeutralDark10,
    inversePrimary = Primary40,
)

@Composable
fun ConnectAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = ConnectAppTypography,
        shapes = ConnectAppShapes,
        content = content,
    )
}
