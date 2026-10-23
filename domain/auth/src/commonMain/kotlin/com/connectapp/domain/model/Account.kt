package com.connectapp.domain.model

/**
 * Authenticable identity (docs/00-auditoria-y-plan.md §6). Every account must be linked to its
 * own professional record before reaching Home (D3, confirmed 2026-10-16).
 */
data class Account(
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val professionalId: String?,
) {
    val hasProfessionalProfile: Boolean get() = professionalId != null

    // Personal data never reaches logs or crash reports (docs/00-auditoria-y-plan.md §6).
    override fun toString(): String = "Account(id=$id, professionalId=$professionalId)"
}
