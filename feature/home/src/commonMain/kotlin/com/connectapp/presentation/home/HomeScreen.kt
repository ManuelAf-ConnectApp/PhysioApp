package com.connectapp.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.connectapp.commonresources.adaptive.LocalWindowWidthClass
import com.connectapp.commonresources.adaptive.WindowWidthClass
import com.connectapp.commonresources.home_greeting
import com.connectapp.commonresources.home_welcome_subtitle
import com.connectapp.domain.backup.BackupReminder
import com.connectapp.presentation.home.component.BackupReminderCard
import com.connectapp.presentation.home.component.HomeCard
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** Cards per row: one entity per row on medium windows, two on expanded ones (see [HomeDestination]). */
internal fun homeGridColumns(widthClass: WindowWidthClass): Int = when (widthClass) {
    WindowWidthClass.COMPACT -> 1
    WindowWidthClass.MEDIUM -> 2
    WindowWidthClass.EXPANDED -> 4
}

// Keeps the cards readable on 13" tablets in landscape instead of stretching them edge to edge.
private val HomeMaxContentWidth = 1200.dp

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onNavigate: (HomeDestination) -> Unit,
    onOpenBackup: () -> Unit,
) {
    val viewModel: HomeViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is HomeEffect.NavigateTo -> onNavigate(effect.destination)
                HomeEffect.OpenBackup -> onOpenBackup()
            }
        }
    }

    // No statusBarsPadding() here — MainScreen's Scaffold (the app shell) already reserves that
    // space once for every screen; adding it again here would double it under the TopBar.
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        HomeScreenContent(
            state = state,
            columns = homeGridColumns(LocalWindowWidthClass.current),
            onIntent = viewModel::onIntent,
        )
    }
}

@Composable
private fun HomeScreenContent(
    state: HomeState,
    columns: Int,
    onIntent: (HomeIntent) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = Modifier.widthIn(max = HomeMaxContentWidth).fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            HomeHeader(firstName = state.firstName)
        }

        val reminder = state.backupReminder
        if (reminder is BackupReminder.Due) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                BackupReminderCard(
                    lastBackupAt = reminder.lastBackupAt,
                    onMakeBackup = { onIntent(HomeIntent.BackupReminderClicked) },
                )
            }
        }

        items(state.destinations) { destination ->
            HomeCard(
                destination = destination,
                onClick = { onIntent(HomeIntent.DestinationClicked(destination)) },
            )
        }
    }
}

@Composable
private fun HomeHeader(firstName: String?) {
    Column(modifier = Modifier.padding(bottom = 8.dp)) {
        if (firstName != null) {
            Text(
                text = stringResource(home_greeting, firstName),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.semantics { heading() },
            )
        }
        Text(
            text = stringResource(home_welcome_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
