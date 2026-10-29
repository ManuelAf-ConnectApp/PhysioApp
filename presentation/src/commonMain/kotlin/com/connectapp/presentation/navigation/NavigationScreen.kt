package com.connectapp.presentation.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.connectapp.presentation.MainViewModel

/** Renders the back stack owned by [MainViewModel]. */
@Composable
fun NavigationScreen(
    modifier: Modifier = Modifier,
    navState: NavigationState,
    /** System back; the shell may ask before leaving a screen with unsaved changes. */
    onBack: () -> Unit,
    entryProvider: (Any) -> NavEntry<out Any>,
) {
    @Suppress("UNCHECKED_CAST")
    val typedEntryProvider = entryProvider as (Any) -> NavEntry<Any>

    NavDisplay(
        modifier = modifier,
        backStack = navState.backStack,
        onBack = onBack,
        // Each screen on the stack gets its own ViewModels, cleared when it is popped: reopening
        // a form starts empty instead of showing what was typed (passwords included) last time.
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        // A clinical workflow benefits from an immediate, unambiguous change of context. The
        // default NavDisplay transition composes outgoing and incoming screens together, which
        // made forms and records briefly appear on top of one another on both platforms.
        transitionSpec = { EnterTransition.None togetherWith ExitTransition.None },
        popTransitionSpec = { EnterTransition.None togetherWith ExitTransition.None },
        predictivePopTransitionSpec = { _ ->
            EnterTransition.None togetherWith ExitTransition.None
        },
        entryProvider = typedEntryProvider,
    )
}
