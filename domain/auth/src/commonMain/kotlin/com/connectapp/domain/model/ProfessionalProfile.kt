package com.connectapp.domain.model

import com.connectapp.domain.shared.professional.ProfessionalCollege

/** Professional record linked to the signed-in account (D3): what reports and invoices are signed with. */
data class ProfessionalProfile(
    val id: String,
    val nif: String,
    val college: ProfessionalCollege,
    /** As the professional typed it (trimmed); unique within [college]. */
    val licenseNumber: String,
    val firstName: String,
    val lastName: String,
) {
    // Personal data never reaches logs or crash reports (docs/00-auditoria-y-plan.md §6).
    override fun toString(): String = "ProfessionalProfile(id=$id)"
}
