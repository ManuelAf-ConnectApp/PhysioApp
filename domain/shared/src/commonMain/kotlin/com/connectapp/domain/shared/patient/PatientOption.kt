package com.connectapp.domain.shared.patient

import com.connectapp.domain.shared.validation.IdentityDocumentType

/** An active patient offered by the patient picker of new reports and receipts (D8). */
data class PatientOption(
    val id: String,
    val firstName: String,
    val lastName: String,
    val documentType: IdentityDocumentType,
    val documentNumber: String,
) {
    val displayName: String get() = "$firstName $lastName · $documentNumber"

    override fun toString(): String = "PatientOption(id=$id)"
}
