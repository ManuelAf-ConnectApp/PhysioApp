package com.connectapp.presentation.invoices.form

import com.connectapp.domain.invoices.InvoiceField
import com.connectapp.domain.invoices.InvoiceValidation
import com.connectapp.domain.invoices.PaymentMethod
import com.connectapp.domain.shared.patient.PatientOption
import org.jetbrains.compose.resources.StringResource

data class InvoiceFormState(
    /** Patient picker: what is typed and the matching active patients. */
    val patientQuery: String = "",
    val suggestions: List<PatientOption> = emptyList(),
    val selectedPatient: PatientOption? = null,
    /** The professional of the session; never editable. */
    val authorName: String = "",
    val concept: String = "",
    /** The suggested concept ("Sesión de fisioterapia"): leaving it as is is not a change. */
    val defaultConcept: String = "",
    val amountText: String = "",
    val paid: Boolean = false,
    val paymentMethod: PaymentMethod? = null,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: StringResource? = null,
) {
    val invalidFields: Set<InvoiceField>
        get() = InvoiceValidation.errors(selectedPatient?.id, concept, amountText)

    val canSubmit: Boolean get() = invalidFields.isEmpty() && !isSaving && !isSaved

    val hasUnsavedChanges: Boolean
        get() = !isSaved &&
            (selectedPatient != null || amountText.isNotBlank() || paid || concept != defaultConcept)

    // Personal and payment data never reach logs.
    override fun toString(): String = "InvoiceFormState(isSaving=$isSaving, isSaved=$isSaved)"
}

sealed interface InvoiceFormIntent {
    data class PatientQueryChanged(val query: String) : InvoiceFormIntent
    data class PatientSelected(val patient: PatientOption) : InvoiceFormIntent
    object PatientCleared : InvoiceFormIntent

    /** The localized default concept, sent once by the screen. */
    data class DefaultConceptLoaded(val concept: String) : InvoiceFormIntent
    data class ConceptChanged(val concept: String) : InvoiceFormIntent
    data class AmountChanged(val amount: String) : InvoiceFormIntent
    data class PaidChanged(val paid: Boolean) : InvoiceFormIntent
    data class PaymentMethodChanged(val method: PaymentMethod?) : InvoiceFormIntent

    /** "Guardar" or, with [export], "Guardar y exportar PDF". */
    data class SaveClicked(val export: Boolean) : InvoiceFormIntent
}

sealed interface InvoiceFormEffect {
    data class Saved(val invoiceId: String, val export: Boolean) : InvoiceFormEffect
}
