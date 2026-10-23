package com.connectapp.core.uitest

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import org.koin.compose.KoinIsolatedContext
import org.koin.core.module.Module
import org.koin.dsl.koinApplication

/**
 * Shows [content] inside a Koin context that holds only [modules]: typically the feature's own
 * modules plus one binding its repository to a fake, so `koinViewModel()` in the screen resolves
 * through the real wiring. The context is isolated: unlike `KoinApplication { }`, it never
 * becomes the global one that later tests would inherit.
 */
@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.setContentWithKoin(vararg modules: Module, content: @Composable () -> Unit) {
    val koin = koinApplication { modules(*modules) }
    setContent {
        KoinIsolatedContext(context = koin) { content() }
    }
}

/** The editable text field labelled [label]. */
@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.textField(label: String): SemanticsNodeInteraction =
    onNode(hasSetTextAction() and hasText(label))

/** The clickable element (button, row, card) showing [text], never a text field. */
@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.clickable(text: String): SemanticsNodeInteraction =
    onNode(hasClickAction() and hasText(text) and !hasSetTextAction())

/**
 * Waits, up to [timeoutMillis] of real time, for a node showing [text]: for content that appears
 * after a coroutine delay (e.g. debounced suggestions), which `waitForIdle()` does not wait for.
 */
@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.waitForText(
    text: String,
    substring: Boolean = false,
    timeoutMillis: Long = 5_000,
): SemanticsNodeInteraction {
    val matcher = hasText(text, substring = substring)
    waitUntil(timeoutMillis = timeoutMillis) { onAllNodes(matcher).fetchSemanticsNodes().isNotEmpty() }
    return onNode(matcher)
}
