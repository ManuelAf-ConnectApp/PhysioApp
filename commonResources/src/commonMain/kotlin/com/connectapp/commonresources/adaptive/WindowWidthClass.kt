package com.connectapp.commonresources.adaptive

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Material 3 window width classes. PhysioApp targets tablets only (D12, confirmed 2026-10-20):
 * [EXPANDED] is landscape on every tablet and portrait on the largest ones (iPad Pro 13"),
 * [MEDIUM] is portrait on most iPads and 10–11" Android tablets, and [COMPACT] only appears in
 * iPad Split View / Android multi-window. Narrower widths stack panes instead of getting a
 * phone-specific design.
 */
enum class WindowWidthClass {
    COMPACT,
    MEDIUM,
    EXPANDED,
    ;

    companion object {
        val MEDIUM_MIN_WIDTH: Dp = 600.dp
        val EXPANDED_MIN_WIDTH: Dp = 840.dp

        fun fromWidth(width: Dp): WindowWidthClass = when {
            width >= EXPANDED_MIN_WIDTH -> EXPANDED
            width >= MEDIUM_MIN_WIDTH -> MEDIUM
            else -> COMPACT
        }
    }
}

/** Width class of the app window. Provided once by the shell; defaults to [WindowWidthClass.EXPANDED]. */
val LocalWindowWidthClass = staticCompositionLocalOf { WindowWidthClass.EXPANDED }

/**
 * Measures the space available to [content] and provides its [WindowWidthClass] through
 * [LocalWindowWidthClass]. Measuring the layout (not the screen) keeps it correct in iPad Split
 * View and Android multi-window, where the window is narrower than the display.
 */
@Composable
fun ProvideWindowWidthClass(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    BoxWithConstraints(modifier = modifier) {
        CompositionLocalProvider(LocalWindowWidthClass provides WindowWidthClass.fromWidth(maxWidth)) {
            content()
        }
    }
}
