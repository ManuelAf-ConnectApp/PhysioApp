package com.connectapp.domain.patients

import com.connectapp.domain.shared.validation.IdentityDocumentType

/** A patient record. [signature] describes the latest signed consent (D13). */
data class Patient(
    val id: String,
    val documentType: IdentityDocumentType,
    val documentNumber: String,
    val firstName: String,
    val lastName: String,
    val address: String?,
    val email: String?,
    val phone: String?,
    val notes: String?,
    val isArchived: Boolean,
    val hasClinicalRecords: Boolean,
    val signature: SignatureInfo?,
) {
    /** D8: reports and invoices make the record undeletable; it can still be archived. */
    val canDelete: Boolean get() = !hasClinicalRecords

    // Personal and health data never reach logs or crash reports (docs/00-auditoria-y-plan.md §6).
    override fun toString(): String = "Patient(id=$id, isArchived=$isArchived)"
}

/** One search result ("resultados resumidos"). */
data class PatientSummary(
    val id: String,
    val firstName: String,
    val lastName: String,
    val documentType: IdentityDocumentType,
    val documentNumber: String,
    val email: String?,
    val isArchived: Boolean,
) {
    override fun toString(): String = "PatientSummary(id=$id)"
}

/** One active criterion at a time ("NIF" covers every document type). */
enum class PatientSearchCriterion { DOCUMENT, FULL_NAME, EMAIL }

/** Form input of "Registrar paciente" and of its edit mode, before validation. */
data class PatientDraft(
    val documentType: IdentityDocumentType = IdentityDocumentType.NIF_NIE,
    val documentNumber: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val address: String = "",
    val email: String = "",
    val phone: String = "",
    val notes: String = "",
) {
    override fun toString(): String = "PatientDraft(documentType=$documentType)"
}

fun Patient.toDraft() = PatientDraft(
    documentType = documentType,
    documentNumber = documentNumber,
    firstName = firstName,
    lastName = lastName,
    address = address.orEmpty(),
    email = email.orEmpty(),
    phone = phone.orEmpty(),
    notes = notes.orEmpty(),
)

/** The only failure type a [PatientRepository] result may carry. */
sealed class PatientError : Exception() {
    object InvalidInput : PatientError()

    /** Another patient already has this document (same type and number). */
    object DocumentAlreadyRegistered : PatientError()
    object NotFound : PatientError()

    /** Has clinical reports or invoices: can be archived but not deleted (D8). */
    object HasClinicalRecords : PatientError()

    data class Unknown(override val cause: Throwable) : PatientError()
}
