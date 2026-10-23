package com.connectapp.commonresources.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.connectapp.commonresources.button_change
import com.connectapp.commonresources.label_patient
import com.connectapp.commonresources.patient_picker_hint
import com.connectapp.commonresources.patient_picker_no_matches
import org.jetbrains.compose.resources.stringResource

/** One patient as the picker shows it: name as [title] and document as [subtitle]. */
data class PickerItem(val id: String, val title: String, val subtitle: String) {
    override fun toString(): String = "PickerItem(id=$id)"
}

/** What the patient picker shows: the typed [query], its [suggestions] or the [selected] patient. */
data class PatientPickerState(
    val query: String = "",
    val suggestions: List<PickerItem> = emptyList(),
    val selected: PickerItem? = null,
) {
    override fun toString(): String = "PatientPickerState(suggestions=${suggestions.size})"
}

/**
 * "selector con autocompletado/sugerencias filtradas desde la base local a
 * medida que se escribe". Reports and receipts keep the chosen patient's id, never the typed text.
 */
@Composable
fun PatientPicker(
    state: PatientPickerState,
    onQueryChange: (String) -> Unit,
    onSelect: (id: String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val selected = state.selected
    if (selected != null) {
        OutlinedCard(modifier = modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(label_patient),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text("${selected.title} · ${selected.subtitle}", style = MaterialTheme.typography.bodyLarge)
                }
                TextButton(onClick = onClear) { Text(stringResource(button_change)) }
            }
        }
        return
    }
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = state.query,
            onValueChange = onQueryChange,
            label = { Text(stringResource(label_patient)) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            supportingText = { Text(stringResource(patient_picker_hint)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
            modifier = Modifier.fillMaxWidth(),
        )
        if (state.query.isNotBlank()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                if (state.suggestions.isEmpty()) {
                    Text(
                        text = stringResource(patient_picker_no_matches),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp),
                    )
                }
                state.suggestions.forEachIndexed { index, item ->
                    if (index > 0) HorizontalDivider()
                    ListItem(
                        headlineContent = { Text(item.title) },
                        supportingContent = { Text(item.subtitle) },
                        modifier = Modifier.clickable(role = Role.Button) { onSelect(item.id) },
                    )
                }
            }
        }
    }
}
