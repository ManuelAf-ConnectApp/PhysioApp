package com.connectapp.presentation.invoices.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FindInPage
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.connectapp.commonresources.adaptive.ListDetailLayout
import com.connectapp.commonresources.adaptive.LocalWindowWidthClass
import com.connectapp.commonresources.adaptive.WindowWidthClass
import com.connectapp.commonresources.badge_annulled
import com.connectapp.commonresources.badge_paid
import com.connectapp.commonresources.badge_unpaid
import com.connectapp.commonresources.close
import com.connectapp.commonresources.component.EmptyState
import com.connectapp.commonresources.format.formatDateTime
import com.connectapp.commonresources.invoice_search_no_results_message
import com.connectapp.commonresources.invoice_select_title
import com.connectapp.commonresources.label_invoice_number
import com.connectapp.commonresources.report_select_message
import com.connectapp.commonresources.search_by
import com.connectapp.commonresources.search_criterion_patient_document
import com.connectapp.commonresources.search_criterion_patient_name
import com.connectapp.commonresources.search_field_label
import com.connectapp.commonresources.search_no_results_title
import com.connectapp.core.mvi.StateViewModel
import com.connectapp.domain.invoices.InvoiceSearchCriterion
import com.connectapp.domain.invoices.InvoiceSummary
import com.connectapp.domain.invoices.SearchInvoicesUseCase
import com.connectapp.presentation.invoices.detail.InvoiceDetail
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

data class InvoiceSearchState(
    val criterion: InvoiceSearchCriterion = InvoiceSearchCriterion.PATIENT_NAME,
    val query: String = "",
    val results: List<InvoiceSummary> = emptyList(),
    val isLoading: Boolean = true,
    /** Receipt shown in the detail pane when list and detail fit side by side. */
    val selectedId: String? = null,
) {
    override fun toString(): String = "InvoiceSearchState(criterion=$criterion, results=${results.size})"
}

sealed interface InvoiceSearchIntent {
    /** One criterion at a time, so changing it clears the query. */
    data class CriterionSelected(val criterion: InvoiceSearchCriterion) : InvoiceSearchIntent
    data class QueryChanged(val query: String) : InvoiceSearchIntent
    data class ResultSelected(val invoiceId: String?) : InvoiceSearchIntent
}

/** "Buscar factura", newest first; voided receipts are listed and marked. */
@OptIn(ExperimentalCoroutinesApi::class)
class InvoiceSearchViewModel(
    searchInvoices: SearchInvoicesUseCase,
) : StateViewModel<InvoiceSearchState, InvoiceSearchIntent>(InvoiceSearchState()) {

    init {
        state
            .map { it.criterion to it.query }
            .distinctUntilChanged()
            .flatMapLatest { (criterion, query) -> searchInvoices(criterion, query) }
            .onEach { results -> updateState { it.copy(results = results, isLoading = false) } }
            .launchIn(viewModelScope)
    }

    override fun reduce(intent: InvoiceSearchIntent) {
        when (intent) {
            is InvoiceSearchIntent.CriterionSelected ->
                updateState { it.copy(criterion = intent.criterion, query = "") }
            is InvoiceSearchIntent.QueryChanged -> updateState { it.copy(query = intent.query) }
            is InvoiceSearchIntent.ResultSelected -> updateState { it.copy(selectedId = intent.invoiceId) }
        }
    }
}

private val InvoiceSearchCriterion.label: StringResource
    get() = when (this) {
        InvoiceSearchCriterion.NUMBER -> label_invoice_number
        InvoiceSearchCriterion.PATIENT_NAME -> search_criterion_patient_name
        InvoiceSearchCriterion.PATIENT_DOCUMENT -> search_criterion_patient_document
    }

/**
 * "Buscar factura". On expanded windows the receipt opens next to the list; otherwise
 * [onOpenDetail] opens it as its own screen with a back arrow (agreed 2026-10-20).
 */
@Composable
fun InvoiceSearchScreen(
    onOpenDetail: (invoiceId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: InvoiceSearchViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val twoPanes = LocalWindowWidthClass.current == WindowWidthClass.EXPANDED
    val selectedId = state.selectedId

    ListDetailLayout(
        hasSelection = twoPanes && selectedId != null,
        modifier = modifier,
        list = {
            SearchPane(
                state = state,
                onIntent = viewModel::onIntent,
                onResultClick = { id ->
                    if (twoPanes) viewModel.onIntent(InvoiceSearchIntent.ResultSelected(id)) else onOpenDetail(id)
                },
            )
        },
        detail = { if (selectedId != null) InvoiceDetail(invoiceId = selectedId) },
        emptyDetail = {
            EmptyState(
                icon = Icons.AutoMirrored.Outlined.ReceiptLong,
                title = stringResource(invoice_select_title),
                message = stringResource(report_select_message),
            )
        },
    )
}

@Composable
private fun SearchPane(
    state: InvoiceSearchState,
    onIntent: (InvoiceSearchIntent) -> Unit,
    onResultClick: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(search_by), style = MaterialTheme.typography.labelLarge)
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                val criteria = InvoiceSearchCriterion.entries
                criteria.forEachIndexed { index, criterion ->
                    SegmentedButton(
                        selected = state.criterion == criterion,
                        onClick = { onIntent(InvoiceSearchIntent.CriterionSelected(criterion)) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = criteria.size),
                        label = { Text(stringResource(criterion.label), maxLines = 1) },
                    )
                }
            }
            // A single field for the active criterion (the other two cannot be used at once).
            OutlinedTextField(
                value = state.query,
                onValueChange = { onIntent(InvoiceSearchIntent.QueryChanged(it)) },
                label = { Text("${stringResource(search_field_label)}: ${stringResource(state.criterion.label)}") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = if (state.query.isNotEmpty()) {
                    {
                        IconButton(onClick = { onIntent(InvoiceSearchIntent.QueryChanged("")) }) {
                            Icon(Icons.Filled.Clear, contentDescription = stringResource(close))
                        }
                    }
                } else {
                    null
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = if (state.criterion == InvoiceSearchCriterion.PATIENT_NAME) {
                        KeyboardCapitalization.Words
                    } else {
                        KeyboardCapitalization.Characters
                    },
                    imeAction = ImeAction.Search,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        HorizontalDivider()
        if (!state.isLoading && state.results.isEmpty()) {
            EmptyState(
                icon = Icons.Outlined.FindInPage,
                title = stringResource(search_no_results_title),
                message = stringResource(invoice_search_no_results_message),
            )
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(state.results, key = { it.id }) { invoice ->
                    ResultRow(
                        invoice = invoice,
                        selected = invoice.id == state.selectedId,
                        onClick = { onResultClick(invoice.id) },
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun ResultRow(invoice: InvoiceSummary, selected: Boolean, onClick: () -> Unit) {
    ListItem(
        overlineContent = { Text("${invoice.number} · ${formatDateTime(invoice.issuedAtEpochMillis)}") },
        headlineContent = { Text("${invoice.patientName} · ${invoice.patientDocument}") },
        supportingContent = { Text("${invoice.professionalName} · ${invoice.amount.format()}") },
        trailingContent = {
            when {
                invoice.isVoided -> Text(stringResource(badge_annulled), color = MaterialTheme.colorScheme.error)
                invoice.paid -> Text(stringResource(badge_paid))
                else -> Text(stringResource(badge_unpaid), color = MaterialTheme.colorScheme.tertiary)
            }
        },
        colors = if (selected) {
            ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        } else {
            ListItemDefaults.colors()
        },
        modifier = Modifier.selectable(selected = selected, role = Role.Button, onClick = onClick),
    )
}
