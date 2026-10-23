package com.connectapp.presentation.professionals.search

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
import androidx.compose.material.icons.outlined.Badge
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.connectapp.commonresources.adaptive.ListDetailLayout
import com.connectapp.commonresources.adaptive.LocalWindowWidthClass
import com.connectapp.commonresources.adaptive.WindowWidthClass
import com.connectapp.commonresources.badge_archived
import com.connectapp.commonresources.close
import com.connectapp.commonresources.component.EmptyState
import com.connectapp.commonresources.professional_select_message
import com.connectapp.commonresources.professional_select_title
import com.connectapp.commonresources.search_by
import com.connectapp.commonresources.search_criterion_full_name
import com.connectapp.commonresources.search_criterion_license
import com.connectapp.commonresources.search_criterion_nif
import com.connectapp.commonresources.search_field_label
import com.connectapp.commonresources.search_no_results_message
import com.connectapp.commonresources.search_no_results_title
import com.connectapp.commonresources.search_show_archived
import com.connectapp.domain.professionals.ProfessionalSearchCriterion
import com.connectapp.domain.professionals.ProfessionalSummary
import com.connectapp.presentation.professionals.detail.ProfessionalDetail
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

private val ProfessionalSearchCriterion.label: StringResource
    get() = when (this) {
        ProfessionalSearchCriterion.NIF -> search_criterion_nif
        ProfessionalSearchCriterion.FULL_NAME -> search_criterion_full_name
        ProfessionalSearchCriterion.LICENSE_NUMBER -> search_criterion_license
    }

/**
 * "Buscar profesional". On expanded windows the record opens next to the
 * list; otherwise [onOpenDetail] opens it as its own screen with a back arrow (agreed 2026-10-20).
 */
@Composable
fun ProfessionalSearchScreen(
    onOpenDetail: (professionalId: String) -> Unit,
    onEdit: (professionalId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: ProfessionalSearchViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val twoPanes = LocalWindowWidthClass.current == WindowWidthClass.EXPANDED
    val selectedId = state.selectedId

    ListDetailLayout(
        // Narrower windows open the record as a screen, so the list never hides behind a selection.
        hasSelection = twoPanes && selectedId != null,
        modifier = modifier,
        list = {
            SearchPane(
                state = state,
                onIntent = viewModel::onIntent,
                onResultClick = { id ->
                    if (twoPanes) viewModel.onIntent(ProfessionalSearchIntent.ResultSelected(id)) else onOpenDetail(id)
                },
            )
        },
        detail = {
            if (selectedId != null) {
                ProfessionalDetail(
                    professionalId = selectedId,
                    onEdit = onEdit,
                    onDeleted = { viewModel.onIntent(ProfessionalSearchIntent.ResultSelected(null)) },
                )
            }
        },
        emptyDetail = {
            EmptyState(
                icon = Icons.Outlined.Badge,
                title = stringResource(professional_select_title),
                message = stringResource(professional_select_message),
            )
        },
    )
}

@Composable
private fun SearchPane(
    state: ProfessionalSearchState,
    onIntent: (ProfessionalSearchIntent) -> Unit,
    onResultClick: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(stringResource(search_by), style = MaterialTheme.typography.labelLarge)
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                val criteria = ProfessionalSearchCriterion.entries
                criteria.forEachIndexed { index, criterion ->
                    SegmentedButton(
                        selected = state.criterion == criterion,
                        onClick = { onIntent(ProfessionalSearchIntent.CriterionSelected(criterion)) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = criteria.size),
                        label = { Text(stringResource(criterion.label), maxLines = 1) },
                    )
                }
            }
            // A single field for the active criterion: the other two cannot be edited at the same
            // time ("al usar uno, inhabilitar los otros").
            OutlinedTextField(
                value = state.query,
                onValueChange = { onIntent(ProfessionalSearchIntent.QueryChanged(it)) },
                label = { Text("${stringResource(search_field_label)}: ${stringResource(state.criterion.label)}") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = if (state.query.isNotEmpty()) {
                    {
                        IconButton(onClick = { onIntent(ProfessionalSearchIntent.QueryChanged("")) }) {
                            Icon(Icons.Filled.Clear, contentDescription = stringResource(close))
                        }
                    }
                } else {
                    null
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = if (state.criterion == ProfessionalSearchCriterion.FULL_NAME) {
                        KeyboardCapitalization.Words
                    } else {
                        KeyboardCapitalization.Characters
                    },
                    imeAction = ImeAction.Search,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(role = Role.Switch) {
                        onIntent(ProfessionalSearchIntent.IncludeArchivedChanged(!state.includeArchived))
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
                message = stringResource(search_no_results_message),
            )
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(state.results, key = { it.id }) { professional ->
                    ResultRow(
                        professional = professional,
                        selected = professional.id == state.selectedId,
                        onClick = { onResultClick(professional.id) },
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun ResultRow(professional: ProfessionalSummary, selected: Boolean, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text("${professional.firstName} ${professional.lastName}") },
        supportingContent = {
            Text("${professional.nif} · ${professional.college.displayName} · ${professional.licenseNumber}")
        },
        trailingContent = if (professional.isArchived) {
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
