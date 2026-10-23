package com.connectapp.presentation.reports.form

import androidx.lifecycle.viewModelScope
import com.connectapp.commonresources.error_report_annulled
import com.connectapp.commonresources.error_report_not_author
import com.connectapp.commonresources.report_not_found
import com.connectapp.core.mvi.MviViewModel
import com.connectapp.domain.reports.CreateReportUseCase
import com.connectapp.domain.reports.ObserveReportUseCase
import com.connectapp.domain.reports.SuggestPatientsUseCase
import com.connectapp.domain.reports.UpdateReportUseCase
import com.connectapp.domain.usecase.CurrentAuthorUseCase
import com.connectapp.domain.usecase.ObserveCurrentProfessionalUseCase
import com.connectapp.presentation.reports.toMessage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Pause before searching patients, so typing a name does not query on every letter. */
private const val SUGGESTION_DELAY_MILLIS = 200L

/**
 * "Crear informe" and, with a [reportId], its edit mode for the author
 * (D17). The professional is the one of the session and the patient is picked from active ones.
 */
@Suppress("LongParameterList") // One use case per operation, as in the other forms.
class ReportFormViewModel(
    private val reportId: String?,
    private val createReport: CreateReportUseCase,
    private val updateReport: UpdateReportUseCase,
    private val suggestPatients: SuggestPatientsUseCase,
    observeReport: ObserveReportUseCase,
    currentAuthor: CurrentAuthorUseCase,
    observeCurrentProfessional: ObserveCurrentProfessionalUseCase,
) : MviViewModel<ReportFormState, ReportFormIntent, ReportFormEffect>(
    ReportFormState(isEditing = reportId != null, isLoading = reportId != null),
) {
    private var suggestionJob: Job? = null

    init {
        viewModelScope.launch {
            if (reportId == null) {
                val professional = observeCurrentProfessional().first()
                val name = professional?.let { "${it.firstName} ${it.lastName}" }.orEmpty()
                updateState { it.copy(authorName = name) }
                return@launch
            }
            val report = observeReport(reportId).first()
            val author = currentAuthor()
            updateState { state ->
                when {
                    report == null -> state.copy(isLoading = false, blockedReason = report_not_found)
                    else -> state.copy(
                        isLoading = false,
                        fixedPatientLabel = "${report.patient.firstName} ${report.patient.lastName} · " +
                            report.patient.documentNumber,
                        authorName = "${report.professional.firstName} ${report.professional.lastName}",
                        reportText = report.reportText,
                        conditionText = report.conditionText,
                        savedReportText = report.reportText,
                        savedConditionText = report.conditionText,
                        blockedReason = when {
                            report.professional.id != author -> error_report_not_author
                            report.isAnnulled -> error_report_annulled
                            else -> null
                        },
                    )
                }
            }
        }
    }

    override fun reduce(intent: ReportFormIntent) {
        when (intent) {
            is ReportFormIntent.PatientQueryChanged -> onPatientQuery(intent.query)
            is ReportFormIntent.PatientSelected -> {
                suggestionJob?.cancel()
                updateState { it.copy(selectedPatient = intent.patient, patientQuery = "", suggestions = emptyList()) }
            }
            ReportFormIntent.PatientCleared -> updateState { it.copy(selectedPatient = null) }
            is ReportFormIntent.ReportTextChanged ->
                updateState { it.copy(reportText = intent.text, errorMessage = null) }
            is ReportFormIntent.ConditionTextChanged ->
                updateState { it.copy(conditionText = intent.text, errorMessage = null) }
            ReportFormIntent.SaveClicked -> save()
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

    private fun save() {
        val form = state.value
        if (!form.canSubmit) return
        updateState { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            val result = if (reportId == null) {
                createReport(form.selectedPatient?.id, form.reportText, form.conditionText)
            } else {
                updateReport(reportId, form.reportText, form.conditionText).map { reportId }
            }
            result
                .onSuccess { id ->
                    updateState { it.copy(isSaving = false, isSaved = true) }
                    emitEffect(ReportFormEffect.Saved(id))
                }
                .onFailure { error -> updateState { it.copy(isSaving = false, errorMessage = error.toMessage()) } }
        }
    }
}
