package com.connectapp.commonresources.component

import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Read-only field that opens a list of [options] (closed catalogues such as the professional
 * associations). Like [ValidatedTextField], the [error] is shown only once the user has opened and
 * closed the list without choosing, so an untouched form is not covered in errors.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectField(
    selected: String?,
    options: List<String>,
    onSelected: (index: Int) -> Unit,
    label: StringResource,
    error: StringResource?,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    var touched by remember { mutableStateOf(false) }
    val errorText = error?.takeIf { touched }?.let { stringResource(it) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { open ->
            if (!open) touched = true
            expanded = open
        },
    ) {
        OutlinedTextField(
            value = selected.orEmpty(),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(label)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            isError = errorText != null,
            supportingText = errorText?.let { { Text(it) } },
            singleLine = true,
            modifier = modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .semantics { if (errorText != null) error(errorText) },
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
                touched = true
            },
        ) {
            options.forEachIndexed { index, option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(index)
                        expanded = false
                        touched = true
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}
