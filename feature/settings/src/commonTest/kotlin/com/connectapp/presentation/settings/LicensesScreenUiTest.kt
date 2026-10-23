package com.connectapp.presentation.settings

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.runComposeUiTest
import com.connectapp.commonresources.licenses_public_domain
import com.connectapp.core.uitest.Texts
import com.connectapp.core.uitest.UiTest
import com.connectapp.core.uitest.waitForText
import com.connectapp.presentation.settings.licenses.LicensesScreen
import com.connectapp.presentation.settings.licenses.openSourceLicenses
import kotlin.test.Test

/** The licence notices required by MIT and BSD-3-Clause ship inside the app and can be read. */
@OptIn(ExperimentalTestApi::class)
class LicensesScreenUiTest : UiTest() {

    private val text = Texts()

    @Test
    fun `every licence opens its bundled full text`() = runComposeUiTest {
        setContent { LicensesScreen() }
        // A sentence unique to each bundled text, in the order of the cards.
        val excerpts = mapOf(
            "apache-2.0.txt" to "TERMS AND CONDITIONS FOR USE, REPRODUCTION, AND DISTRIBUTION",
            "mit-kvault.txt" to "Copyright (c) 2020 Liftric GmbH",
            "bsd-3-clause-sqlcipher.txt" to "Copyright (c) 2025, ZETETIC LLC",
            "bsd-3-clause-skia.txt" to "Copyright (c) 2011 Google Inc.",
        )

        openSourceLicenses.filter { it.textFile != null }.forEach { group ->
            val excerpt = excerpts.getValue(checkNotNull(group.textFile))
            val card = hasClickAction() and hasText(group.components.first().name, substring = true)
            // Cards below the viewport are not composed yet: scroll the lazy list to them.
            onNode(hasScrollToIndexAction()).performScrollToNode(card)
            onNode(card).performClick()
            waitForText(excerpt, substring = true).assertExists()
        }
    }

    @Test
    fun `the public domain entry has no licence text to open`() = runComposeUiTest {
        setContent {
            text.resolve(licenses_public_domain)
            LicensesScreen()
        }

        onNode(hasScrollToIndexAction()).performScrollToNode(hasText(text[licenses_public_domain]))
        onNodeWithText(text[licenses_public_domain]).assertExists()
        onNode(hasClickAction() and hasText(text[licenses_public_domain])).assertDoesNotExist()
    }
}
