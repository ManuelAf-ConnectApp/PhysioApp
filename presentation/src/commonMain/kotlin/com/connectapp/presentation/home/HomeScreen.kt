package com.connectapp.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.connectapp.domain.model.User
import com.connectapp.presentation.component.HomeCard
import com.connectapp.designresources.TokensResources
import com.connectapp.designresources.DimensResources
import com.connectapp.presentation.home.component.HomeComponentWidget
import org.jetbrains.compose.resources.stringResource
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    user: User,
    onNavigateToPatient: () -> Unit,
    onNavigateToSearchPatient: () -> Unit,
    onNavigateToProfessional: () -> Unit,
    onNavigateToSearchProfessional: () -> Unit,
    onNavigateToCreateReport: () -> Unit,
    onNavigateToSearchReport: () -> Unit,
    onNavigateToInvoice: () -> Unit,
    onNavigateToSearchInvoice: () -> Unit,
) {
    val viewModel: HomeViewModel = koinViewModel()
    val onIntent = viewModel::onIntent

    val state by viewModel.state.collectAsState()

    LaunchedEffect(user) {
        onIntent(HomeIntent.LoadUser(user))
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is HomeEffect.NavigateToDetail -> {
                    when (effect.item.id) {
                        TokensResources.idNewPatient -> onNavigateToPatient()
                        TokensResources.idEditPatient -> onNavigateToSearchPatient()
                        TokensResources.idSearchPatient -> onNavigateToSearchPatient()
                        TokensResources.idNewProfessional -> onNavigateToProfessional()
                        TokensResources.idSearchProfessional -> onNavigateToSearchProfessional()
                        TokensResources.idCreateReport -> onNavigateToCreateReport()
                        TokensResources.idSearchReport -> onNavigateToSearchReport()
                        TokensResources.idCreateInvoice -> onNavigateToInvoice()
                        TokensResources.idSearchInvoice -> onNavigateToSearchInvoice()
                    }
                }
            }
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = DimensResources.spacing16),
        horizontalArrangement = Arrangement.spacedBy(DimensResources.spacing12),
        verticalArrangement = Arrangement.spacedBy(DimensResources.spacing12),
        contentPadding = PaddingValues(vertical = DimensResources.spacing16)
    ) {
        item(span = { GridItemSpan(2) }) {
            val userName = state.user?.firstName ?: user.firstName
            Column {
                Text(
                    text = stringResource(TokensResources.welcomeBack).replace("%s", userName),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(DimensResources.spacing4))
                Text(
                    text = stringResource(TokensResources.whatsHappeningToday),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(DimensResources.spacing16))
            }
        }

        items(state.items) { item ->
            HomeCard(item) {
                viewModel.onIntent(HomeIntent.ItemClicked(item))
            }
        }

    }

    HomeComponentWidget()
}


