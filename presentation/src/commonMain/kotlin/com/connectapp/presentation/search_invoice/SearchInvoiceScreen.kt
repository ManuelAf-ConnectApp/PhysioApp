/*
Author: Manuel María Alconchel Fernández
E-mail: incidencias@connectapp.es
Date: 30/06/2006

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.connectapp.presentation.search_invoice

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.connectapp.designresources.DimensResources
import com.connectapp.designresources.TokensResources
import com.connectapp.domain.model.Invoice
import com.connectapp.presentation.component.CustomOutlinedTextField
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

    val pdfSaveSuccessMsg = stringResource(TokensResources.pdfSaveSuccess)
    val pdfGenerateErrorMsg = stringResource(TokensResources.pdfGenerateError)

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is SearchInvoiceEffect.ShowSnackBar -> {
                    snackBarHostState.showSnackbar(effect.message)
                }

                is SearchInvoiceEffect.SuccessGeneratePdf -> {
                    val message = when (effect.message) {
                        "pdf_save_success" -> pdfSaveSuccessMsg
                        else -> effect.message
                    }
                    snackBarHostState.showSnackbar(message = message)
                }

                is SearchInvoiceEffect.ErrorGeneratePdf -> {
                    val message = when (effect.message) {
                        "pdf_generate_error" -> pdfGenerateErrorMsg
                        else -> effect.message
                    }
                    snackBarHostState.showSnackbar(message = message)
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
                text = stringResource(TokensResources.invoiceNumber),
                isSelected = state.searchType == SearchType.INVOICE_NUMBER,
                onClick = { onIntent(SearchInvoiceIntent.OnSearchTypeChange(SearchType.INVOICE_NUMBER)) },
                modifier = Modifier.weight(1f)
            )
            SearchTypeChip(
                text = stringResource(TokensResources.professional),
                isSelected = state.searchType == SearchType.PROFESSIONAL,
                onClick = { onIntent(SearchInvoiceIntent.OnSearchTypeChange(SearchType.PROFESSIONAL)) },
                modifier = Modifier.weight(1f)
            )
            SearchTypeChip(
                text = stringResource(TokensResources.patient),
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
            Text(stringResource(TokensResources.search))
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
                InvoiceItem(
                    invoice = invoice,
                    onClick = { onIntent(SearchInvoiceIntent.OnInvoiceSelected(invoice)) }
                )
            }

            item {
                state.selectedInvoice?.let { invoice ->
                    Spacer(modifier = Modifier.height(DimensResources.spacing32))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(DimensResources.spacing16))
                    InvoiceDetails(
                        invoice = invoice,
                        onGeneratePdfClick = { onIntent(SearchInvoiceIntent.OnGeneratePDFClick) }
                    )
                }
            }
        }
    }
}

@Composable
private fun InvoiceDetails(
    invoice: Invoice,
    onGeneratePdfClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(DimensResources.spacing8)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(TokensResources.invoiceDetails),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Button(onClick = onGeneratePdfClick) {
                Text(stringResource(TokensResources.generatePdf))
            }
        }

        Spacer(modifier = Modifier.height(DimensResources.spacing16))

        DetailItem(label = stringResource(TokensResources.invoiceNumber), value = invoice.invoiceNumber)
        DetailItem(label = stringResource(TokensResources.pdfDateLabel), value = invoice.date)
        DetailItem(label = stringResource(TokensResources.concept), value = invoice.concept)
        DetailItem(label = stringResource(TokensResources.amountEuro), value = "${invoice.amount}€")
        DetailItem(
            label = stringResource(TokensResources.pdfStatusLabel),
            value = if (invoice.isPaid) stringResource(TokensResources.pdfPaidLabel) else stringResource(TokensResources.pdfPendingPaymentLabel)
        )
    }
}

@Composable
private fun DetailItem(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = DimensResources.spacing4)) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
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
