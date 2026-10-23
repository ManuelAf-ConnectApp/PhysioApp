package com.connectapp.presentation.invoices.form

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.connectapp.commonresources.button_save
import com.connectapp.commonresources.button_save_and_export
import com.connectapp.commonresources.component.ErrorMessage
import com.connectapp.commonresources.component.LoadingIndicator
import com.connectapp.commonresources.component.PatientPicker
import com.connectapp.commonresources.component.PatientPickerState
import com.connectapp.commonresources.component.PickerItem
import com.connectapp.commonresources.component.RecordField
import com.connectapp.commonresources.component.SelectField
import com.connectapp.commonresources.component.ValidatedTextField
import com.connectapp.commonresources.error_invalid_amount
import com.connectapp.commonresources.error_invalid_concept
import com.connectapp.commonresources.invoice_default_concept
import com.connectapp.commonresources.label_amount
import com.connectapp.commonresources.label_concept
import com.connectapp.commonresources.label_paid
import com.connectapp.commonresources.label_payment_method
import com.connectapp.commonresources.label_professional
import com.connectapp.commonresources.navigation.GuardUnsavedChanges
import com.connectapp.commonresources.payment_method_none
import com.connectapp.domain.invoices.InvoiceField
import com.connectapp.domain.invoices.PaymentMethod
import com.connectapp.domain.shared.patient.PatientOption
import com.connectapp.presentation.invoices.detail.NotFiscalNotice
import com.connectapp.presentation.invoices.label
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** "Crear factura": a payment receipt (D6). [onSaved] says whether the PDF was asked for too. */
@Composable
fun InvoiceFormScreen(
    onSaved: (invoiceId: String, export: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: InvoiceFormViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val onIntent = viewModel::onIntent
    val defaultConcept = stringResource(invoice_default_concept)

    GuardUnsavedChanges(dirty = state.hasUnsavedChanges)

    LaunchedEffect(defaultConcept) { onIntent(InvoiceFormIntent.DefaultConceptLoaded(defaultConcept)) }
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is InvoiceFormEffect.Saved -> onSaved(effect.invoiceId, effect.export)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .widthIn(max = 720.dp)
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            NotFiscalNotice()
            RecordField(label_professional, state.authorName.ifBlank { null })
            PatientPicker(
                state = PatientPickerState(
                    query = state.patientQuery,
                    suggestions = state.suggestions.map { it.toPickerItem() },
                    selected = state.selectedPatient?.toPickerItem(),
                ),
                onQueryChange = { onIntent(InvoiceFormIntent.PatientQueryChanged(it)) },
                onSelect = { id ->
                    val patient = state.suggestions.firstOrNull { it.id == id }
                    if (patient != null) onIntent(InvoiceFormIntent.PatientSelected(patient))
                },
                onClear = { onIntent(InvoiceFormIntent.PatientCleared) },
            )
            ValidatedTextField(
                value = state.concept,
                onValueChange = { onIntent(InvoiceFormIntent.ConceptChanged(it)) },
                label = label_concept,
                error = error_invalid_concept.takeIf { InvoiceField.CONCEPT in state.invalidFields },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                modifier = Modifier.fillMaxWidth(),
            )
            ValidatedTextField(
                value = state.amountText,
                onValueChange = { onIntent(InvoiceFormIntent.AmountChanged(it)) },
                label = label_amount,
                error = error_invalid_amount.takeIf { InvoiceField.AMOUNT in state.invalidFields },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
            )
            PaymentFields(state = state, onIntent = onIntent)
            state.errorMessage?.let {
                ErrorMessage(stringResource(it))
            }
            if (state.isSaving) {
                LoadingIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                SaveButtons(enabled = state.canSubmit, onSave = { onIntent(InvoiceFormIntent.SaveClicked(it)) })
            }
        }
    }
}

@Composable
private fun PaymentFields(state: InvoiceFormState, onIntent: (InvoiceFormIntent) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(
                value = state.paid,
                role = Role.Checkbox,
                onValueChange = { onIntent(InvoiceFormIntent.PaidChanged(it)) },
            )
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = state.paid, onCheckedChange = null)
        Text(stringResource(label_paid), style = MaterialTheme.typography.bodyLarge)
    }
    if (state.paid) {
        val methods = listOf<PaymentMethod?>(null) + PaymentMethod.entries
        val labels = methods.map { stringResource(it?.label ?: payment_method_none) }
        SelectField(
            selected = labels[methods.indexOf(state.paymentMethod)],
            options = labels,
            onSelected = { index -> onIntent(InvoiceFormIntent.PaymentMethodChanged(methods[index])) },
            label = label_payment_method,
            error = null,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun SaveButtons(enabled: Boolean, onSave: (export: Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedButton(
            onClick = { onSave(false) },
            enabled = enabled,
            modifier = Modifier.weight(1f).heightIn(min = 56.dp),
            shape = MaterialTheme.shapes.large,
        ) {
            Text(stringResource(button_save), style = MaterialTheme.typography.titleMedium)
        }
        Button(
            onClick = { onSave(true) },
            enabled = enabled,
            modifier = Modifier.weight(1f).heightIn(min = 56.dp),
            shape = MaterialTheme.shapes.large,
        ) {
            Text(stringResource(button_save_and_export), style = MaterialTheme.typography.titleMedium)
        }
    }
}

private fun PatientOption.toPickerItem() = PickerItem(id, "$firstName $lastName", documentNumber)
