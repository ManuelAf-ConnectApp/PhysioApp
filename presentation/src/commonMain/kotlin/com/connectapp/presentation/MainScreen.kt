package com.connectapp.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavEntry
import com.connectapp.commonresources.adaptive.ProvideWindowWidthClass
import com.connectapp.commonresources.button_discard
import com.connectapp.commonresources.button_keep_editing
import com.connectapp.commonresources.discard_changes_message
import com.connectapp.commonresources.discard_changes_title
import com.connectapp.commonresources.export.PurgeExportedDocumentsEffect
import com.connectapp.commonresources.navigation.LocalUnsavedChangesGuard
import com.connectapp.commonresources.navigation.UnsavedChangesGuard
import com.connectapp.commonresources.session_expired_inactivity
import com.connectapp.presentation.component.CustomBottomBar
import com.connectapp.presentation.component.CustomTopBar
import com.connectapp.presentation.navigation.NavigationScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

// SnackbarHostState.showSnackbar() defaults to SnackbarDuration.Indefinite whenever an
// actionLabel is passed (every snackbar here has one, e.g. Home's "close" action) — so without
// an explicit timed dismissal it would stay on screen until the user taps the action.
private const val SNACKBAR_AUTO_DISMISS_MILLIS = 5_000L

// How often the session idle timeout (D14, 5 min) is checked while the app is in the foreground.
private const val IDLE_CHECK_INTERVAL_MILLIS = 10_000L

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    entryProvider: (Any) -> NavEntry<out Any>,
) {
    val viewModel: MainViewModel = koinViewModel()
    val navState by viewModel.navState.collectAsStateWithLifecycle()
    val snackBarState by viewModel.snackBarState.collectAsStateWithLifecycle()
    val snackBarHostState = remember { SnackbarHostState() }

    val currentRoute = navState.route

    // Session idle timeout (D14): checked periodically, whenever the app comes back to the
    // foreground and before any interaction extends the session (MainViewModel.onUserActivity).
    LaunchedEffect(Unit) {
        while (true) {
            delay(IDLE_CHECK_INTERVAL_MILLIS)
            viewModel.checkIdleSession()
        }
    }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.checkIdleSession() }

    val sessionExpiredNotice by viewModel.sessionExpiredNotice.collectAsStateWithLifecycle()
    val sessionExpiredMessage = stringResource(session_expired_inactivity)
    LaunchedEffect(sessionExpiredNotice) {
        if (sessionExpiredNotice) {
            viewModel.sessionExpiredNoticeShown()
            viewModel.showSnackBar(sessionExpiredMessage)
        }
    }

    LaunchedEffect(snackBarState) {
        snackBarState?.let { state ->
            val autoDismissJob = launch {
                delay(SNACKBAR_AUTO_DISMISS_MILLIS)
                snackBarHostState.currentSnackbarData?.dismiss()
            }
            val result = snackBarHostState.showSnackbar(
                message = state.message,
                actionLabel = state.actionLabel,
            )
            autoDismissJob.cancel()
            if (result == SnackbarResult.ActionPerformed) {
                state.onAction?.invoke()
            }
            viewModel.dismissSnackBar()
        }
    }

    // A PDF left in the private temporary folder by an export the app could not clean up (D19).
    PurgeExportedDocumentsEffect()

    // Phase 5 removed the template's 1 s full-screen loader on every iOS navigation (A13): it
    // blocked every screen change to mask NavDisplay's instant transitions under "Reduce Motion"
    // (CMP-10284), which is what that accessibility setting asks for anyway.
    ProvideWindowWidthClass(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .statusBarsPadding()
            // Observes every touch without consuming it, to postpone the idle timeout.
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent(PointerEventPass.Initial)
                        viewModel.onUserActivity()
                    }
                }
            }
            .onPreviewKeyEvent {
                viewModel.onUserActivity()
                false
            }
    ) {
        // Leaving a screen with unsaved changes asks first. The idle timeout
        // does not ask: an expired session must close even with a form half filled in.
        val unsavedChanges = remember { UnsavedChangesGuard() }
        var pendingLeave by remember { mutableStateOf<(() -> Unit)?>(null) }
        val leave = { action: () -> Unit ->
            if (unsavedChanges.hasUnsavedChanges) pendingLeave = action else action()
        }
        CompositionLocalProvider(LocalUnsavedChangesGuard provides unsavedChanges) {
            Scaffold(
                snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
                topBar = {
                    if (currentRoute.showTopBar) {
                        CustomTopBar(
                            title = stringResource(currentRoute.titleRes),
                            onBack = if (currentRoute.showTopBarBackArrow) ({ leave(viewModel::onBack) }) else null,
                        )
                    }
                },
                bottomBar = {
                    if (currentRoute.showBottomBar) {
                        CustomBottomBar(
                            currentRoute = currentRoute,
                            onNavigate = { route -> leave { viewModel.updateNavigationState(route) } }
                        )
                    }
                }
            ) { paddingValues ->
                NavigationScreen(
                    modifier = modifier.padding(paddingValues = paddingValues),
                    navState = navState,
                    onBack = { leave(viewModel::onBack) },
                    entryProvider = entryProvider
                )
            }
        }

        pendingLeave?.let { action ->
            DiscardChangesDialog(
                onDiscard = {
                    pendingLeave = null
                    unsavedChanges.discardAll()
                    action()
                },
                onKeepEditing = { pendingLeave = null },
            )
        }
    }
}

@Composable
private fun DiscardChangesDialog(onDiscard: () -> Unit, onKeepEditing: () -> Unit) {
    AlertDialog(
        onDismissRequest = onKeepEditing,
        title = { Text(stringResource(discard_changes_title)) },
        text = { Text(stringResource(discard_changes_message)) },
        confirmButton = {
            TextButton(
                onClick = onDiscard,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
            ) {
                Text(stringResource(button_discard))
            }
        },
        dismissButton = { TextButton(onClick = onKeepEditing) { Text(stringResource(button_keep_editing)) } },
    )
}
