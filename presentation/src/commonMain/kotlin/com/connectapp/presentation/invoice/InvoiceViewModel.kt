package com.connectapp.presentation.invoice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.connectapp.domain.model.Invoice
import com.connectapp.domain.usecase.GenerateInvoicePdfUseCase
import com.connectapp.domain.usecase.GetAllProfessionalsUseCase
import com.connectapp.domain.usecase.GetSearchPatientsByNameUseCase
import com.connectapp.domain.usecase.SaveInvoiceUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class InvoiceViewModel(
    private val generateInvoicePdfUseCase: GenerateInvoicePdfUseCase,
    private val saveInvoiceUseCase: SaveInvoiceUseCase,
    private val getSearchPatientsByNameUseCase: GetSearchPatientsByNameUseCase,
    private val getAllProfessionalsUseCase: GetAllProfessionalsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(InvoiceState())
    val state: StateFlow<InvoiceState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<InvoiceEffect>()
    val effect: SharedFlow<InvoiceEffect> = _effect.asSharedFlow()

    init {
        generateInvoiceNumber()
        loadProfessionals()
    }

    private fun loadProfessionals() {
        val professionals = getAllProfessionalsUseCase()
        _state.update { it.copy(professionals = professionals) }
    }

    private fun generateInvoiceNumber() {
        val chars = ('A'..'Z') + ('0'..'9')
        val number = (1..8)
            .map { chars.random() }
            .joinToString("")
        _state.update { it.copy(invoiceNumber = "INV-$number") }
    }

    fun onIntent(intent: InvoiceIntent) {
        when (intent) {
            is InvoiceIntent.PatientNameChanged -> {
                val suggestions = getSearchPatientsByNameUseCase(intent.name)
                _state.update {
                    it.copy(
                        patientName = intent.name,
                        suggestedPatients = suggestions,
                        selectedPatient = null
                    )
                }
            }

            is InvoiceIntent.PatientSelected -> {
                _state.update {
                    it.copy(
                        patientName = "${intent.patient.firstName} ${intent.patient.lastName}",
                        patientId = intent.patient.id.toString(),
                        suggestedPatients = emptyList(),
                        selectedPatient = intent.patient
                    )
                }
            }

            is InvoiceIntent.InvoiceNumberChanged -> _state.update { it.copy(invoiceNumber = intent.number) }
            is InvoiceIntent.DateChanged -> _state.update { it.copy(date = intent.date) }
            is InvoiceIntent.AmountChanged -> _state.update { it.copy(amount = intent.amount) }
            is InvoiceIntent.ConceptChanged -> _state.update { it.copy(concept = intent.concept) }
            is InvoiceIntent.PaidStatusChanged -> _state.update { it.copy(isPaid = intent.isPaid) }
            is InvoiceIntent.ProfessionalSelected -> _state.update { it.copy(professionalId = intent.professionalId) }
            InvoiceIntent.SaveClicked -> saveInvoice()
            InvoiceIntent.AddPatientClicked -> emitEffect(InvoiceEffect.NavigateToCreatePatient)
            InvoiceIntent.BackClicked -> emitEffect(InvoiceEffect.NavigateBack)
        }
    }

    private fun saveInvoice() {
        viewModelScope.launch {
            val invoice = Invoice(
                patientId = state.value.patientId,
                professionalId = state.value.professionalId,
                invoiceNumber = state.value.invoiceNumber,
                date = state.value.date,
                amount = state.value.amount,
                concept = state.value.concept,
                isPaid = state.value.isPaid
            )

            saveInvoiceUseCase(invoice).onSuccess {
                generateInvoicePdfUseCase(invoice = invoice).onSuccess {
                    emitEffect(InvoiceEffect.ShowSuccess("Invoice saved successfully"))
                    emitEffect(InvoiceEffect.NavigateBack)

                } .onFailure {
                    emitEffect(InvoiceEffect.ShowError(it.message ?: "Unknown error"))
                }
            }.onFailure {
                _effect.emit(InvoiceEffect.ShowError(it.message ?: "Unknown error"))
            }

        }
    }

    private fun emitEffect(effect: InvoiceEffect) {
        viewModelScope.launch {
            _effect.emit(effect)
        }
    }
}
