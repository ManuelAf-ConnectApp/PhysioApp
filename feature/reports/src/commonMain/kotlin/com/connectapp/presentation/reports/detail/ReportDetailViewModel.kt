package com.connectapp.presentation.reports.detail

import androidx.lifecycle.viewModelScope
import com.connectapp.core.mvi.StateViewModel
import com.connectapp.domain.reports.AnnulReportUseCase
import com.connectapp.domain.reports.ClinicalReport
import com.connectapp.domain.reports.ObserveReportUseCase
import com.connectapp.domain.reports.ObserveReportVersionsUseCase
import com.connectapp.domain.reports.ReportValidation
import com.connectapp.domain.reports.ReportVersion
import com.connectapp.domain.usecase.CurrentAuthorUseCase
import com.connectapp.presentation.reports.toMessage
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource

data class ReportDetailState(
    val report: ClinicalReport? = null,
    val versions: List<ReportVersion> = emptyList(),
    /** The signed-in professional wrote it: only then edit and annul are offered (D17). */
    val isAuthor: Boolean = false,
    val isLoading: Boolean = true,
    val showVersions: Boolean = false,
    /** Non-null while the annulment dialog is open: the reason being typed. */
    val annulmentReason: String? = null,
    /** The PDF warning with its share/save choice is open (D19). */
    val isExporting: Boolean = false,
    val errorMessage: StringResource? = null,
) {
    val canChange: Boolean get() = isAuthor && report?.isAnnulled == false
    val canConfirmAnnulment: Boolean get() = annulmentReason?.let(ReportValidation::isValidAnnulmentReason) == true

    override fun toString(): String = "ReportDetailState(isLoading=$isLoading, isAuthor=$isAuthor)"
}

sealed interface ReportDetailIntent {
    object VersionsToggled : ReportDetailIntent
    object AnnulClicked : ReportDetailIntent
    data class AnnulmentReasonChanged(val reason: String) : ReportDetailIntent
    object AnnulmentConfirmed : ReportDetailIntent
    object AnnulmentDismissed : ReportDetailIntent
    object ExportClicked : ReportDetailIntent
    object ExportDismissed : ReportDetailIntent
}

/** A report with its earlier versions; anyone may export it and its author may annul it with a reason (D17). */
class ReportDetailViewModel(
    private val reportId: String,
    observeReport: ObserveReportUseCase,
    observeVersions: ObserveReportVersionsUseCase,
    currentAuthor: CurrentAuthorUseCase,
    private val annulReport: AnnulReportUseCase,
) : StateViewModel<ReportDetailState, ReportDetailIntent>(ReportDetailState()) {

    init {
        combine(
            observeReport(reportId),
            observeVersions(reportId),
            currentAuthor.observe(),
        ) { report, versions, author -> Triple(report, versions, author) }
            .onEach { (report, versions, author) ->
                updateState {
                    it.copy(
                        report = report,
                        versions = versions,
                        isAuthor = report != null && report.professional.id == author,
                        isLoading = false,
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    override fun reduce(intent: ReportDetailIntent) {
        when (intent) {
            ReportDetailIntent.VersionsToggled -> updateState { it.copy(showVersions = !it.showVersions) }
            ReportDetailIntent.AnnulClicked -> updateState { it.copy(annulmentReason = "", errorMessage = null) }
            is ReportDetailIntent.AnnulmentReasonChanged -> updateState { it.copy(annulmentReason = intent.reason) }
            ReportDetailIntent.AnnulmentDismissed -> updateState { it.copy(annulmentReason = null) }
            ReportDetailIntent.AnnulmentConfirmed -> annul()
            ReportDetailIntent.ExportClicked -> updateState { it.copy(isExporting = true) }
            ReportDetailIntent.ExportDismissed -> updateState { it.copy(isExporting = false) }
        }
    }

    private fun annul() {
        val reason = state.value.annulmentReason ?: return
        if (!state.value.canConfirmAnnulment) return
        updateState { it.copy(annulmentReason = null) }
        viewModelScope.launch {
            annulReport(reportId, reason).onFailure { error ->
                updateState { it.copy(errorMessage = error.toMessage()) }
            }
        }
    }
}
