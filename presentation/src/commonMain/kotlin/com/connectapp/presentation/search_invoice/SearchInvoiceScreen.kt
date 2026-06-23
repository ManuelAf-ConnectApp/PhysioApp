package com.connectapp.presentation.search_invoice

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.connectapp.designresources.TokensResources
import com.connectapp.designresources.DimensResources
import com.connectapp.presentation.component.CustomOutlinedTextField
import com.connectapp.presentation.component.CustomTopBar
import com.connectapp.presentation.search_invoice.component.InvoiceItem
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SearchInvoiceScreen(
    modifier: Modifier = Modifier,
    snackBarHostState: SnackbarHostState,
    onNavigateBack: () -> Unit,
) {
    val viewModel: SearchInvoiceViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val onIntent = viewModel::onIntent

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is SearchInvoiceEffect.ShowSnackBar -> {
                    snackBarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    if (state.isLoading) {
        Box(modifier = modifier.fillMaxSize()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    } else {
        SearchInvoiceScreenContent(
            modifier = modifier.fillMaxSize(),
            state = state,
            onIntent = onIntent,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchInvoiceScreenContent(
    modifier: Modifier = Modifier,
    state: SearchInvoiceState,
    onIntent: (SearchInvoiceIntent) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(DimensResources.spacing16)
    ) {
        // Search Type Selection
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(DimensResources.spacing8)
        ) {
            SearchTypeChip(
                text = "Number",
                isSelected = state.searchType == SearchType.INVOICE_NUMBER,
                onClick = { onIntent(SearchInvoiceIntent.OnSearchTypeChange(SearchType.INVOICE_NUMBER)) },
                modifier = Modifier.weight(1f)
            )
            SearchTypeChip(
                text = "Professional",
                isSelected = state.searchType == SearchType.PROFESSIONAL,
                onClick = { onIntent(SearchInvoiceIntent.OnSearchTypeChange(SearchType.PROFESSIONAL)) },
                modifier = Modifier.weight(1f)
            )
            SearchTypeChip(
                text = "Patient",
                isSelected = state.searchType == SearchType.PATIENT,
                onClick = { onIntent(SearchInvoiceIntent.OnSearchTypeChange(SearchType.PATIENT)) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(DimensResources.spacing16))

        CustomOutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = state.searchQuery,
            onValueChange = { onIntent(SearchInvoiceIntent.OnSearchQueryChange(it)) },
            label = when (state.searchType) {
                SearchType.INVOICE_NUMBER -> stringResource(TokensResources.invoiceNumber)
                SearchType.PROFESSIONAL -> stringResource(TokensResources.professional)
                SearchType.PATIENT -> stringResource(TokensResources.patient)
            }
        )

        Spacer(modifier = Modifier.height(DimensResources.spacing16))

        Button(
            onClick = { onIntent(SearchInvoiceIntent.OnSearchClick) },
            modifier = Modifier.fillMaxWidth(),
            enabled = state.searchQuery.isNotEmpty()
        ) {
            Text("Search")
        }

        Spacer(modifier = Modifier.height(DimensResources.spacing16))

        state.error?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(DimensResources.spacing16))
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(DimensResources.spacing8),
            contentPadding = PaddingValues(bottom = DimensResources.spacing16)
        ) {
            items(state.invoices) { invoice ->
                InvoiceItem(invoice = invoice)
            }
        }
    }
}

@Composable
private fun SearchTypeChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Text(
                text = text,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        },
        modifier = modifier
    )
}
