package com.connectapp.presentation.invoices.form

import androidx.lifecycle.viewModelScope
import com.connectapp.core.mvi.MviViewModel
import com.connectapp.domain.invoices.CreateInvoiceUseCase
import com.connectapp.domain.invoices.SuggestInvoicePatientsUseCase
import com.connectapp.domain.usecase.ObserveCurrentProfessionalUseCase
import com.connectapp.presentation.invoices.toMessage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Pause before searching patients, so typing a name does not query on every letter. */
private const val SUGGESTION_DELAY_MILLIS = 200L

/**
 * "Crear factura", a payment receipt under D6: the professional is the one of
 * the session, the patient is picked from active ones and the number is assigned on saving.
 * Receipts are not edited afterwards: only their collection status changes (detail screen).
 */
class InvoiceFormViewModel(
    private val createInvoice: CreateInvoiceUseCase,
    private val suggestPatients: SuggestInvoicePatientsUseCase,
    observeCurrentProfessional: ObserveCurrentProfessionalUseCase,
) : MviViewModel<InvoiceFormState, InvoiceFormIntent, InvoiceFormEffect>(InvoiceFormState()) {

    private var suggestionJob: Job? = null

    init {
        viewModelScope.launch {
            val professional = observeCurrentProfessional().first()
            val name = professional?.let { "${it.firstName} ${it.lastName}" }.orEmpty()
            updateState { it.copy(authorName = name) }
        }
    }

    override fun reduce(intent: InvoiceFormIntent) {
        when (intent) {
            is InvoiceFormIntent.PatientQueryChanged -> onPatientQuery(intent.query)
            is InvoiceFormIntent.PatientSelected -> {
                suggestionJob?.cancel()
                updateState { it.copy(selectedPatient = intent.patient, patientQuery = "", suggestions = emptyList()) }
            }
            InvoiceFormIntent.PatientCleared -> updateState { it.copy(selectedPatient = null) }
            is InvoiceFormIntent.DefaultConceptLoaded -> updateState {
                // Only before the user has typed a concept of their own.
                if (it.concept == it.defaultConcept) {
                    it.copy(concept = intent.concept, defaultConcept = intent.concept)
                } else {
                    it.copy(defaultConcept = intent.concept)
                }
            }
            is InvoiceFormIntent.ConceptChanged ->
                updateState { it.copy(concept = intent.concept, errorMessage = null) }
            is InvoiceFormIntent.AmountChanged ->
                updateState { it.copy(amountText = intent.amount, errorMessage = null) }
            is InvoiceFormIntent.PaidChanged ->
                updateState { it.copy(paid = intent.paid, paymentMethod = it.paymentMethod.takeIf { intent.paid }) }
            is InvoiceFormIntent.PaymentMethodChanged -> updateState { it.copy(paymentMethod = intent.method) }
            is InvoiceFormIntent.SaveClicked -> save(intent.export)
        }
    }

    private fun onPatientQuery(query: String) {
        updateState { it.copy(patientQuery = query) }
        suggestionJob?.cancel()
        suggestionJob = viewModelScope.launch {
            delay(SUGGESTION_DELAY_MILLIS)
            val suggestions = suggestPatients(query)
            updateState { it.copy(suggestions = suggestions) }
        }
    }

    private fun save(export: Boolean) {
        val form = state.value
        if (!form.canSubmit) return
        updateState { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            createInvoice(form.selectedPatient?.id, form.concept, form.amountText, form.paid, form.paymentMethod)
                .onSuccess { id ->
                    updateState { it.copy(isSaving = false, isSaved = true) }
                    emitEffect(InvoiceFormEffect.Saved(id, export))
                }
                .onFailure { error -> updateState { it.copy(isSaving = false, errorMessage = error.toMessage()) } }
        }
    }
}
