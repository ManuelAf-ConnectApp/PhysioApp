package com.connectapp.presentation.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.connectapp.domain.model.Report
import com.connectapp.domain.usecase.GetAllProfessionalsUseCase
import com.connectapp.domain.usecase.GetSearchPatientsByNameUseCase
import com.connectapp.domain.usecase.SaveReportUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class ReportViewModel(
    private val getSearchPatientsByNameUseCase: GetSearchPatientsByNameUseCase,
    private val getSearchProfessionalListUseCase: GetAllProfessionalsUseCase,
    private val saveReportUseCase: SaveReportUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ReportState.EMPTY)
    val state: StateFlow<ReportState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<ReportEffect>()
    val effect: SharedFlow<ReportEffect> = _effect.asSharedFlow()

    private var searchJob: Job? = null

    init {
        _state.update {
            ReportState.EMPTY
        }

        loadProfessionals()
    }

    private fun loadProfessionals() {
        viewModelScope.launch {
            val professionals = getSearchProfessionalListUseCase()
            _state.update { it.copy(professionals = professionals) }
        }
    }

    fun onIntent(intent: ReportIntent) {
        when (intent) {
            is ReportIntent.Initialize -> loadReport(intent.reportId)
            is ReportIntent.ProfessionalSelected -> {
                _state.update { it.copy(selectedProfessional = intent.professional) }
            }

            is ReportIntent.PatientNameChanged -> {
                _state.update { it.copy(patientName = intent.name) }
                searchPatients(intent.name)
            }

            is ReportIntent.PatientSelected -> {
                _state.update {
                    it.copy(
                        patientName = "${intent.patient.firstName} ${intent.patient.lastName}",
                        selectedPatient = intent.patient,
                        suggestedPatients = emptyList()
                    )
                }
            }

            is ReportIntent.ReportTitleChanged -> _state.update { it.copy(reportTitle = intent.title) }
            is ReportIntent.DateChanged -> _state.update { it.copy(date = intent.date) }
            is ReportIntent.ClinicalContentChanged -> _state.update { it.copy(clinicalContent = intent.clinicalContent) }
            is ReportIntent.DiagnosisChanged -> _state.update { it.copy(diagnosis = intent.diagnosis) }
            is ReportIntent.TreatmentChanged -> _state.update { it.copy(treatment = intent.treatment) }
            ReportIntent.SaveClicked -> saveReport()
            ReportIntent.AddPatientClicked -> emitEffect(ReportEffect.NavigateToCreatePatient)
            ReportIntent.BackClicked -> emitEffect(ReportEffect.NavigateBack)
        }
    }

    private fun searchPatients(query: String) {
        searchJob?.cancel()
        if (query.length < 3) {
            _state.update { it.copy(suggestedPatients = emptyList()) }
            return
        }

        searchJob = viewModelScope.launch {
            delay(duration = 300.milliseconds)
            val patients = getSearchPatientsByNameUseCase(query)
            _state.update { it.copy(suggestedPatients = patients) }
        }
    }

    private fun loadReport(reportId: String?) {
        if (reportId == null) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            // Simulating loading data
            delay(800.milliseconds)
            val currentState = _state.value
            _state.update {
                it.copy(
                    isLoading = false,
                    selectedProfessional = currentState.professionals.firstOrNull(),
                    patientName = "Juan Pérez",
                    reportTitle = "Evaluación de esguince de tobillo",
                    date = "15/10/2023",
                    clinicalContent = "Paciente presenta inflamación en el maleolo externo tras torcedura jugando al fútbol.",
                    diagnosis = "Esguince de tobillo grado II",
                    treatment = "Crioterapia, vendaje funcional y reposo activo durante 48 horas."
                )
            }
        }
    }

    private fun saveReport() {
        _state.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            val report = Report(
                title = state.value.reportTitle,
                date = state.value.date,
                clinicalContent = state.value.clinicalContent,
                diagnosis = state.value.diagnosis,
                treatment = state.value.treatment,
                codPatient = state.value.selectedPatient?.id ?: 0L,
                codProfessional = state.value.selectedProfessional?.id ?: 0L
            )
            saveReportUseCase(report = report).onSuccess { isSaved ->
                if (!isSaved) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Error al guardar el reporte"
                        )
                    }
                    emitEffect(ReportEffect.ShowError("Error al guardar el reporte"))
                } else {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                    emitEffect(ReportEffect.ShowMessage("Reporte guardado correctamente"))
                    emitEffect(ReportEffect.NavigateBack)
                }
            }
            _state.update { it.copy(isLoading = false, isSaved = true) }
            emitEffect(ReportEffect.NavigateBack)
        }
    }

    private fun emitEffect(effect: ReportEffect) {
        viewModelScope.launch {
            _effect.emit(effect)
        }
    }
}
