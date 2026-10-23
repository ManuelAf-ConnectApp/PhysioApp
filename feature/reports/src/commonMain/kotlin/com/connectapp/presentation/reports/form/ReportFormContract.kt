package com.connectapp.presentation.reports.form

import com.connectapp.domain.reports.ReportField
import com.connectapp.domain.reports.ReportValidation
import com.connectapp.domain.shared.patient.PatientOption
import org.jetbrains.compose.resources.StringResource

data class ReportFormState(
    val isEditing: Boolean = false,
    /** Patient picker (create only): what is typed and the matching active patients. */
    val patientQuery: String = "",
    val suggestions: List<PatientOption> = emptyList(),
    val selectedPatient: PatientOption? = null,
    /** Patient shown in edit mode, where it cannot change. */
    val fixedPatientLabel: String? = null,
    /** The professional of the session (create) or the author (edit); never editable. */
    val authorName: String = "",
    val reportText: String = "",
    val conditionText: String = "",
    val savedReportText: String = "",
    val savedConditionText: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    /** Set when the report cannot be edited (not the author, annulled, not found). */
    val blockedReason: StringResource? = null,
    val errorMessage: StringResource? = null,
) {
    val invalidFields: Set<ReportField>
        get() = ReportValidation.errors(
            patientId = if (isEditing) "unchanged" else selectedPatient?.id,
            reportText = reportText,
            conditionText = conditionText,
        )

    val canSubmit: Boolean
        get() = invalidFields.isEmpty() && !isSaving && !isLoading && blockedReason == null

    val hasUnsavedChanges: Boolean
        get() {
            val textChanged = reportText != savedReportText || conditionText != savedConditionText
            return !isSaved && blockedReason == null && (textChanged || (!isEditing && selectedPatient != null))
        }

    // Clinical data never reaches logs.
    override fun toString(): String = "ReportFormState(isEditing=$isEditing, isSaving=$isSaving)"
}

sealed interface ReportFormIntent {
    data class PatientQueryChanged(val query: String) : ReportFormIntent
    data class PatientSelected(val patient: PatientOption) : ReportFormIntent
    object PatientCleared : ReportFormIntent
    data class ReportTextChanged(val text: String) : ReportFormIntent
    data class ConditionTextChanged(val text: String) : ReportFormIntent
    object SaveClicked : ReportFormIntent
}

sealed interface ReportFormEffect {
    data class Saved(val reportId: String) : ReportFormEffect
}
