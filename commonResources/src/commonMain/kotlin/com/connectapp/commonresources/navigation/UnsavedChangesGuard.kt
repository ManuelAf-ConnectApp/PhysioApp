package com.connectapp.commonresources.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Knows whether any screen on display holds unsaved changes ("evitar perder
 * cambios sin confirmación"). Screens report through [GuardUnsavedChanges]; the shell asks
 * [hasUnsavedChanges] before leaving with the back arrow, the system back or the bottom bar.
 * Several owners may be registered while a screen transition shows two screens at once.
 */
class UnsavedChangesGuard {
    private val dirtyOwners = mutableStateMapOf<Any, Unit>()

    val hasUnsavedChanges: Boolean get() = dirtyOwners.isNotEmpty()

    fun report(owner: Any, dirty: Boolean) {
        if (dirty) dirtyOwners[owner] = Unit else dirtyOwners.remove(owner)
    }

    /** The user chose to discard: every pending change is dropped. */
    fun discardAll() = dirtyOwners.clear()
}

val LocalUnsavedChangesGuard = staticCompositionLocalOf { UnsavedChangesGuard() }

/** Reports [dirty] for as long as the calling screen is on display. */
@Composable
fun GuardUnsavedChanges(dirty: Boolean) {
    val guard = LocalUnsavedChangesGuard.current
    val owner = remember { Any() }
    SideEffect { guard.report(owner, dirty) }
    DisposableEffect(guard, owner) {
        onDispose { guard.report(owner, false) }
    }
}
