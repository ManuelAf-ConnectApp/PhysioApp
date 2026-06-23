package com.connectapp.presentation.edit_invoice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.connectapp.domain.usecase.GetAllProfessionalsUseCase
import com.connectapp.domain.usecase.GetSearchPatientsByNameUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EditInvoiceViewModel(
    private val getSearchPatientsByNameUseCase: GetSearchPatientsByNameUseCase,
    private val getAllProfessionalsUseCase: GetAllProfessionalsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(EditInvoiceState())
    val state get() = _state.asStateFlow()

    private val _effect = MutableSharedFlow<EditInvoiceEffect>()
    val effect get() = _effect.asSharedFlow()

    init {
        loadProfessionals()
    }

    private fun loadProfessionals() {
        val professionals = getAllProfessionalsUseCase()
        _state.update { it.copy(professionals = professionals) }
    }

    fun onIntent(intent: EditInvoiceIntent) {
        when (intent) {
            is EditInvoiceIntent.LoadInvoice -> loadInvoice(intent.invoiceId)
            is EditInvoiceIntent.PatientNameChanged -> {
                val suggestions = getSearchPatientsByNameUseCase(intent.name)
                _state.update { it.copy(patientName = intent.name, suggestedPatients = suggestions) }
            }
            is EditInvoiceIntent.PatientSelected -> {
                _state.update { 
                    it.copy(
                        patientName = "${intent.patient.firstName} ${intent.patient.lastName}",
                        patientId = intent.patient.id.toString(),
                        suggestedPatients = emptyList()
                    )
                }
            }
            is EditInvoiceIntent.DateChanged -> _state.update { it.copy(date = intent.date) }
            is EditInvoiceIntent.ConceptChanged -> _state.update { it.copy(concept = intent.concept) }
            is EditInvoiceIntent.AmountChanged -> _state.update { it.copy(amount = intent.amount) }
            is EditInvoiceIntent.PaidStatusChanged -> _state.update { it.copy(isPaid = intent.isPaid) }
            is EditInvoiceIntent.ProfessionalSelected -> _state.update { it.copy(professionalId = intent.professionalId) }
            EditInvoiceIntent.SaveClicked -> saveInvoice()
            EditInvoiceIntent.BackClicked -> {
                viewModelScope.launch {
                    _effect.emit(EditInvoiceEffect.NavigateBack)
                }
            }
        }
    }

    private fun loadInvoice(invoiceId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            // TODO: Load invoice from repository using invoiceId
            // Simulating load for now
            _state.update { 
                it.copy(
                    isLoading = false,
                    invoiceNumber = invoiceId,
                    date = "2023-10-27",
                    concept = "Physical Therapy Session",
                    amount = "50.00",
                    isPaid = true
                ) 
            }
        }
    }

    private fun saveInvoice() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            // TODO: Implement save logic using repository
            _state.update { it.copy(isLoading = false) }
            _effect.emit(EditInvoiceEffect.NavigateBack)
        }
    }
}
