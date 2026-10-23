package com.connectapp.domain.reports

import com.connectapp.domain.shared.validation.IdentityDocumentType

/** A clinical report with the names it is shown with. */
data class ClinicalReport(
    val id: String,
    val code: String,
    val patient: ReportPatient,
    val professional: ReportProfessional,
    val reportText: String,
    val conditionText: String,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
    val annulment: ReportAnnulment?,
    /** Earlier versions kept by the edits (D17). */
    val versionCount: Int,
) {
    val isAnnulled: Boolean get() = annulment != null
    val wasEdited: Boolean get() = versionCount > 0

    // Clinical data never reaches logs or crash reports (docs/00-auditoria-y-plan.md §6).
    override fun toString(): String = "ClinicalReport(id=$id, code=$code, isAnnulled=$isAnnulled)"
}

data class ReportPatient(
    val id: String,
    val firstName: String,
    val lastName: String,
    val documentType: IdentityDocumentType,
    val documentNumber: String,
) {
    override fun toString(): String = "ReportPatient(id=$id)"
}

data class ReportProfessional(val id: String, val firstName: String, val lastName: String) {
    override fun toString(): String = "ReportProfessional(id=$id)"
}

data class ReportAnnulment(val reason: String, val annulledAtEpochMillis: Long) {
    override fun toString(): String = "ReportAnnulment(annulledAt=$annulledAtEpochMillis)"
}

/** The text of a report as it was between [savedAtEpochMillis] and [replacedAtEpochMillis]. */
data class ReportVersion(
    val reportText: String,
    val conditionText: String,
    val savedAtEpochMillis: Long,
    val replacedAtEpochMillis: Long,
) {
    override fun toString(): String = "ReportVersion(savedAt=$savedAtEpochMillis)"
}

/** One search result ("lista de coincidencias"). */
data class ReportSummary(
    val id: String,
    val code: String,
    val patientName: String,
    val patientDocument: String,
    val professionalName: String,
    val createdAtEpochMillis: Long,
    val isAnnulled: Boolean,
) {
    override fun toString(): String = "ReportSummary(id=$id, code=$code)"
}

/** One active criterion ("NIF del paciente" covers every document type). */
enum class ReportSearchCriterion { CODE, PATIENT_NAME, PATIENT_DOCUMENT }

/** The only failure type of [ReportRepository] results and the report use cases. */
sealed class ReportError : Exception() {
    object InvalidInput : ReportError()
    object NotFound : ReportError()

    /** The signed-in account has no professional record or no session (D3). */
    object NotAuthenticated : ReportError()

    /** Only the author edits or annuls a report (D5, D17). */
    object NotAuthor : ReportError()
    object Annulled : ReportError()

    /** The patient does not exist or is archived (D8). */
    object PatientNotAvailable : ReportError()

    data class Unknown(override val cause: Throwable) : ReportError()
}
