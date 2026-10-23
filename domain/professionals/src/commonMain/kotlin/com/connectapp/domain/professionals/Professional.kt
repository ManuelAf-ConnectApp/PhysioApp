package com.connectapp.domain.professionals

import com.connectapp.domain.shared.professional.ProfessionalCollege

/**
 * A professional record, distinct from the login account. [hasAccount] and
 * [hasClinicalRecords] decide what can be done with it (D8): a record with an account can be
 * neither archived nor deleted, and one with reports or invoices can be archived but not deleted.
 */
data class Professional(
    val id: String,
    val nif: String,
    val college: ProfessionalCollege,
    val licenseNumber: String,
    val firstName: String,
    val lastName: String,
    val address: String?,
    val email: String?,
    val phone: String?,
    val notes: String?,
    val isArchived: Boolean,
    val hasAccount: Boolean,
    val hasClinicalRecords: Boolean,
) {
    val canArchive: Boolean get() = !hasAccount
    val canDelete: Boolean get() = !hasAccount && !hasClinicalRecords

    // Personal data never reaches logs or crash reports (docs/00-auditoria-y-plan.md §6).
    override fun toString(): String = "Professional(id=$id, isArchived=$isArchived)"
}

/** One search result ("resultados resumidos"). */
data class ProfessionalSummary(
    val id: String,
    val firstName: String,
    val lastName: String,
    val nif: String,
    val college: ProfessionalCollege,
    val licenseNumber: String,
    val isArchived: Boolean,
) {
    override fun toString(): String = "ProfessionalSummary(id=$id)"
}

/** One active criterion at a time. */
enum class ProfessionalSearchCriterion { NIF, FULL_NAME, LICENSE_NUMBER }

/** Form input of "Registrar profesional" and of its edit mode, before validation. */
data class ProfessionalDraft(
    val nif: String = "",
    val college: ProfessionalCollege? = null,
    val licenseNumber: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val address: String = "",
    val email: String = "",
    val phone: String = "",
    val notes: String = "",
) {
    override fun toString(): String = "ProfessionalDraft(college=$college)"
}

fun Professional.toDraft() = ProfessionalDraft(
    nif = nif,
    college = college,
    licenseNumber = licenseNumber,
    firstName = firstName,
    lastName = lastName,
    address = address.orEmpty(),
    email = email.orEmpty(),
    phone = phone.orEmpty(),
    notes = notes.orEmpty(),
)

/** The only failure type a [ProfessionalRepository] result may carry. */
sealed class ProfessionalError : Exception() {
    object InvalidInput : ProfessionalError()
    object NifAlreadyRegistered : ProfessionalError()

    /** Another professional of the same college already has this number (D9). */
    object LicenseNumberAlreadyRegistered : ProfessionalError()
    object NotFound : ProfessionalError()

    /** Linked to an access account: cannot be archived or deleted. */
    object HasAccount : ProfessionalError()

    /** Has clinical reports or invoices: can be archived but not deleted (D8). */
    object HasClinicalRecords : ProfessionalError()

    data class Unknown(override val cause: Throwable) : ProfessionalError()
}
