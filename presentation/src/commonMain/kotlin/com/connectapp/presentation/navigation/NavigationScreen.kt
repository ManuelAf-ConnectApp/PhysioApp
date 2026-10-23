package com.connectapp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.connectapp.presentation.MainViewModel

/** Renders the back stack owned by [MainViewModel]. */
@Composable
fun NavigationScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel,
    /** System back; the shell may ask before leaving a screen with unsaved changes. */
    onBack: () -> Unit,
    entryProvider: (Any) -> NavEntry<out Any>,
) {
    val navState by viewModel.navState.collectAsStateWithLifecycle()

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
        entryProvider = typedEntryProvider
    )
}
