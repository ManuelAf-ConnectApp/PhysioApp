package com.connectapp.domain.invoices

import com.connectapp.domain.shared.money.Money
import com.connectapp.domain.shared.professional.ProfessionalCollege
import com.connectapp.domain.shared.validation.IdentityDocumentType

/**
 * A payment receipt (D6): an internal "justificante de pago", not a fiscal
 * invoice. [issuer] and [recipient] are the data printed on it, copied when it was issued.
 */
data class Invoice(
    val id: String,
    /** Sequential number within its yearly series, e.g. `2026-000001`. */
    val number: String,
    val issuedAtEpochMillis: Long,
    val issuer: InvoiceIssuer,
    val recipient: InvoiceRecipient,
    val concept: String,
    val amount: Money,
    val vatTreatment: VatTreatment,
    val paid: Boolean,
    val paymentMethod: PaymentMethod?,
    val voiding: InvoiceVoiding?,
    val updatedAtEpochMillis: Long,
) {
    val isVoided: Boolean get() = voiding != null

    // Personal and payment data never reach logs (docs/00-auditoria-y-plan.md §6).
    override fun toString(): String = "Invoice(id=$id, number=$number, isVoided=$isVoided)"
}

data class InvoiceIssuer(
    val professionalId: String,
    val firstName: String,
    val lastName: String,
    val nif: String,
    val college: ProfessionalCollege?,
    val licenseNumber: String,
) {
    override fun toString(): String = "InvoiceIssuer(professionalId=$professionalId)"
}

data class InvoiceRecipient(
    val patientId: String,
    val firstName: String,
    val lastName: String,
    val documentType: IdentityDocumentType,
    val documentNumber: String,
) {
    override fun toString(): String = "InvoiceRecipient(patientId=$patientId)"
}

data class InvoiceVoiding(val reason: String, val voidedAtEpochMillis: Long) {
    override fun toString(): String = "InvoiceVoiding(voidedAt=$voidedAtEpochMillis)"
}

/**
 * Why the receipt charges no VAT. Physiotherapy is exempt (art. 20.Uno.3º LIVA); the value is
 * stored so that a future fiscal invoice can add other treatments. [name] is stored: never rename.
 */
enum class VatTreatment { EXEMPT_HEALTHCARE }

/** How a paid receipt was collected (optional). [name] is stored in the database: never rename. */
enum class PaymentMethod { CASH, CARD, TRANSFER, BIZUM, OTHER }

/** One search result ("mostrar resultados"). */
data class InvoiceSummary(
    val id: String,
    val number: String,
    val issuedAtEpochMillis: Long,
    val patientName: String,
    val patientDocument: String,
    val professionalName: String,
    val amount: Money,
    val paid: Boolean,
    val isVoided: Boolean,
) {
    override fun toString(): String = "InvoiceSummary(id=$id, number=$number)"
}

/** One active criterion ("NIF del paciente" covers every document type). */
enum class InvoiceSearchCriterion { NUMBER, PATIENT_NAME, PATIENT_DOCUMENT }

/** What "Crear" sends to the repository, already validated and trimmed. */
data class NewInvoice(
    val patientId: String,
    val concept: String,
    val amount: Money,
    val paid: Boolean,
    val paymentMethod: PaymentMethod?,
) {
    override fun toString(): String = "NewInvoice(paid=$paid)"
}

/** The only failure type of [InvoiceRepository] results and the receipt use cases. */
sealed class InvoiceError : Exception() {
    object InvalidInput : InvoiceError()
    object NotFound : InvoiceError()

    /** The signed-in account has no professional record or no session (D3). */
    object NotAuthenticated : InvoiceError()

    /** Only the author changes or voids a receipt (D5). */
    object NotAuthor : InvoiceError()
    object Voided : InvoiceError()

    /** The patient does not exist or is archived (D8). */
    object PatientNotAvailable : InvoiceError()

    data class Unknown(override val cause: Throwable) : InvoiceError()
}
