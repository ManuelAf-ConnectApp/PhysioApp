package com.connectapp.presentation.patients.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PersonSearch
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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.connectapp.commonresources.adaptive.ListDetailLayout
import com.connectapp.commonresources.adaptive.LocalWindowWidthClass
import com.connectapp.commonresources.adaptive.WindowWidthClass
import com.connectapp.commonresources.badge_archived
import com.connectapp.commonresources.close
import com.connectapp.commonresources.component.EmptyState
import com.connectapp.commonresources.patient_search_no_results_message
import com.connectapp.commonresources.patient_select_title
import com.connectapp.commonresources.professional_select_message
import com.connectapp.commonresources.search_by
import com.connectapp.commonresources.search_criterion_document
import com.connectapp.commonresources.search_criterion_email
import com.connectapp.commonresources.search_criterion_full_name
import com.connectapp.commonresources.search_field_label
import com.connectapp.commonresources.search_no_results_title
import com.connectapp.commonresources.search_show_archived
import com.connectapp.core.mvi.StateViewModel
import com.connectapp.domain.patients.PatientSearchCriterion
import com.connectapp.domain.patients.PatientSummary
import com.connectapp.domain.patients.SearchPatientsUseCase
import com.connectapp.presentation.patients.detail.PatientDetail
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

data class PatientSearchState(
    val criterion: PatientSearchCriterion = PatientSearchCriterion.FULL_NAME,
    val query: String = "",
    val includeArchived: Boolean = false,
    val results: List<PatientSummary> = emptyList(),
    val isLoading: Boolean = true,
    /** Record shown in the detail pane when list and detail fit side by side. */
    val selectedId: String? = null,
) {
    override fun toString(): String = "PatientSearchState(criterion=$criterion, results=${results.size})"
}

sealed interface PatientSearchIntent {
    /** One criterion at a time, so changing it clears the query. */
    data class CriterionSelected(val criterion: PatientSearchCriterion) : PatientSearchIntent
    data class QueryChanged(val query: String) : PatientSearchIntent
    data class IncludeArchivedChanged(val includeArchived: Boolean) : PatientSearchIntent
    data class ResultSelected(val patientId: String?) : PatientSearchIntent
}

/** "Buscar paciente". Results update live after edits, archiving or deletion. */
@OptIn(ExperimentalCoroutinesApi::class)
class PatientSearchViewModel(
    searchPatients: SearchPatientsUseCase,
) : StateViewModel<PatientSearchState, PatientSearchIntent>(PatientSearchState()) {

    init {
        state
            .map { Triple(it.criterion, it.query, it.includeArchived) }
            .distinctUntilChanged()
            .flatMapLatest { (criterion, query, includeArchived) -> searchPatients(criterion, query, includeArchived) }
            .onEach { results ->
                updateState { state ->
                    state.copy(
                        results = results,
                        isLoading = false,
                        selectedId = state.selectedId?.takeIf { id -> results.any { it.id == id } },
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    override fun reduce(intent: PatientSearchIntent) {
        when (intent) {
            is PatientSearchIntent.CriterionSelected ->
                updateState { it.copy(criterion = intent.criterion, query = "") }
            is PatientSearchIntent.QueryChanged -> updateState { it.copy(query = intent.query) }
            is PatientSearchIntent.IncludeArchivedChanged ->
                updateState { it.copy(includeArchived = intent.includeArchived) }
            is PatientSearchIntent.ResultSelected -> updateState { it.copy(selectedId = intent.patientId) }
        }
    }
}

private val PatientSearchCriterion.label: StringResource
    get() = when (this) {
        PatientSearchCriterion.DOCUMENT -> search_criterion_document
        PatientSearchCriterion.FULL_NAME -> search_criterion_full_name
        PatientSearchCriterion.EMAIL -> search_criterion_email
    }

/**
 * "Buscar paciente". On expanded windows the record opens next to the list; otherwise
 * [onOpenDetail] opens it as its own screen with a back arrow (agreed 2026-10-20).
 */
@Composable
fun PatientSearchScreen(
    onOpenDetail: (patientId: String) -> Unit,
    onEdit: (patientId: String) -> Unit,
    onSignAgain: (patientId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: PatientSearchViewModel = koinViewModel()
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
                    if (twoPanes) viewModel.onIntent(PatientSearchIntent.ResultSelected(id)) else onOpenDetail(id)
                },
            )
        },
        detail = {
            if (selectedId != null) {
                PatientDetail(
                    patientId = selectedId,
                    onEdit = onEdit,
                    onSignAgain = onSignAgain,
                    onDeleted = { viewModel.onIntent(PatientSearchIntent.ResultSelected(null)) },
                )
            }
        },
        emptyDetail = {
            EmptyState(
                icon = Icons.Outlined.Person,
                title = stringResource(patient_select_title),
                message = stringResource(professional_select_message),
            )
        },
    )
}

@Composable
private fun SearchPane(
    state: PatientSearchState,
    onIntent: (PatientSearchIntent) -> Unit,
    onResultClick: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(search_by), style = MaterialTheme.typography.labelLarge)
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                val criteria = PatientSearchCriterion.entries
                criteria.forEachIndexed { index, criterion ->
                    SegmentedButton(
                        selected = state.criterion == criterion,
                        onClick = { onIntent(PatientSearchIntent.CriterionSelected(criterion)) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = criteria.size),
                        label = { Text(stringResource(criterion.label), maxLines = 1) },
                    )
                }
            }
            // A single field for the active criterion (the other two cannot be used at once).
            OutlinedTextField(
                value = state.query,
                onValueChange = { onIntent(PatientSearchIntent.QueryChanged(it)) },
                label = { Text("${stringResource(search_field_label)}: ${stringResource(state.criterion.label)}") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = if (state.query.isNotEmpty()) {
                    {
                        IconButton(onClick = { onIntent(PatientSearchIntent.QueryChanged("")) }) {
                            Icon(Icons.Filled.Clear, contentDescription = stringResource(close))
                        }
                    }
                } else {
                    null
                },
                singleLine = true,
                keyboardOptions = when (state.criterion) {
                    PatientSearchCriterion.DOCUMENT -> KeyboardOptions(KeyboardCapitalization.Characters)
                    PatientSearchCriterion.FULL_NAME -> KeyboardOptions(KeyboardCapitalization.Words)
                    PatientSearchCriterion.EMAIL -> KeyboardOptions(keyboardType = KeyboardType.Email)
                }.copy(imeAction = ImeAction.Search),
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(role = Role.Switch) {
                        onIntent(PatientSearchIntent.IncludeArchivedChanged(!state.includeArchived))
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(stringResource(search_show_archived), style = MaterialTheme.typography.bodyLarge)
                Switch(checked = state.includeArchived, onCheckedChange = null)
            }
        }
        HorizontalDivider()
        if (!state.isLoading && state.results.isEmpty()) {
            EmptyState(
                icon = Icons.Outlined.PersonSearch,
                title = stringResource(search_no_results_title),
                message = stringResource(patient_search_no_results_message),
            )
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(state.results, key = { it.id }) { patient ->
                    ResultRow(
                        patient = patient,
                        selected = patient.id == state.selectedId,
                        onClick = { onResultClick(patient.id) },
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun ResultRow(patient: PatientSummary, selected: Boolean, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text("${patient.firstName} ${patient.lastName}") },
        supportingContent = { Text(listOfNotNull(patient.documentNumber, patient.email).joinToString(" · ")) },
        trailingContent = if (patient.isArchived) {
            { Text(stringResource(badge_archived), style = MaterialTheme.typography.labelMedium) }
        } else {
            null
        },
        colors = if (selected) {
            ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        } else {
            ListItemDefaults.colors()
        },
        modifier = Modifier.selectable(selected = selected, role = Role.Button, onClick = onClick),
    )
}
