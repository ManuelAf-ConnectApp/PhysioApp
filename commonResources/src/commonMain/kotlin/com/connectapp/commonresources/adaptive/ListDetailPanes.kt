package com.connectapp.commonresources.adaptive

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Which panes a list-detail screen shows. */
enum class ListDetailPanes { LIST_ONLY, DETAIL_ONLY, BOTH }

/**
 * Navigation pattern agreed on 2026-10-20: in [WindowWidthClass.EXPANDED] the list and the detail
 * live side by side, so there is nothing to go back to; narrower windows show one pane at a
 * time and the detail gets a back arrow in the top bar.
 */
fun listDetailPanes(widthClass: WindowWidthClass, hasSelection: Boolean): ListDetailPanes = when {
    widthClass == WindowWidthClass.EXPANDED -> ListDetailPanes.BOTH
    hasSelection -> ListDetailPanes.DETAIL_ONLY
    else -> ListDetailPanes.LIST_ONLY
}

private val ListPaneWidth = 360.dp

/**
 * Search screens of phases 6–9. [emptyDetail] fills the detail pane in two-pane mode while nothing
 * is selected (for example "Select a patient to see their record").
 */
@Composable
fun ListDetailLayout(
    hasSelection: Boolean,
    list: @Composable () -> Unit,
    detail: @Composable () -> Unit,
    emptyDetail: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (listDetailPanes(LocalWindowWidthClass.current, hasSelection)) {
        ListDetailPanes.BOTH -> Row(modifier = modifier.fillMaxSize()) {
            Box(modifier = Modifier.width(ListPaneWidth).fillMaxHeight()) { list() }
            VerticalDivider()
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                if (hasSelection) detail() else emptyDetail()
            }
        }
        ListDetailPanes.DETAIL_ONLY -> Box(modifier = modifier.fillMaxSize()) { detail() }
        ListDetailPanes.LIST_ONLY -> Box(modifier = modifier.fillMaxSize()) { list() }
    }
}
