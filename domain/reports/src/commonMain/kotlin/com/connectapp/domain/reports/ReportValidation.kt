package com.connectapp.domain.reports

enum class ReportField { PATIENT, REPORT_TEXT, CONDITION_TEXT, ANNULMENT_REASON }

/** A patient and both multi-line texts are required. */
object ReportValidation {
    fun errors(patientId: String?, reportText: String, conditionText: String): Set<ReportField> = buildSet {
        if (patientId.isNullOrBlank()) add(ReportField.PATIENT)
        if (reportText.isBlank()) add(ReportField.REPORT_TEXT)
        if (conditionText.isBlank()) add(ReportField.CONDITION_TEXT)
    }

    /** D17: annulling needs a reason. */
    fun isValidAnnulmentReason(reason: String): Boolean = reason.isNotBlank()
}
