package com.connectapp.commonresources.adaptive

import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveLayoutTest {

    @Test
    fun `width classes follow the Material 3 breakpoints`() {
        assertEquals(WindowWidthClass.COMPACT, WindowWidthClass.fromWidth(599.dp))
        assertEquals(WindowWidthClass.MEDIUM, WindowWidthClass.fromWidth(600.dp))
        assertEquals(WindowWidthClass.MEDIUM, WindowWidthClass.fromWidth(839.dp))
        assertEquals(WindowWidthClass.EXPANDED, WindowWidthClass.fromWidth(840.dp))
    }

    @Test
    fun `iPads are medium in portrait and expanded in landscape`() {
        // iPad (10th gen): 820 x 1180 pt. iPad Pro 13": 1032 x 1376 pt, expanded in both.
        assertEquals(WindowWidthClass.MEDIUM, WindowWidthClass.fromWidth(820.dp))
        assertEquals(WindowWidthClass.EXPANDED, WindowWidthClass.fromWidth(1180.dp))
        assertEquals(WindowWidthClass.EXPANDED, WindowWidthClass.fromWidth(1032.dp))
    }

    @Test
    fun `expanded windows show list and detail side by side with or without selection`() {
        assertEquals(ListDetailPanes.BOTH, listDetailPanes(WindowWidthClass.EXPANDED, hasSelection = false))
        assertEquals(ListDetailPanes.BOTH, listDetailPanes(WindowWidthClass.EXPANDED, hasSelection = true))
    }

    @Test
    fun `narrower windows show the list until something is selected and then only the detail`() {
        listOf(WindowWidthClass.COMPACT, WindowWidthClass.MEDIUM).forEach { widthClass ->
            assertEquals(ListDetailPanes.LIST_ONLY, listDetailPanes(widthClass, hasSelection = false))
            assertEquals(ListDetailPanes.DETAIL_ONLY, listDetailPanes(widthClass, hasSelection = true))
        }
    }
}
