package com.connectapp.presentation.invoices

import com.connectapp.domain.invoices.Invoice
import com.connectapp.domain.invoices.InvoiceError
import com.connectapp.domain.invoices.InvoiceIssuer
import com.connectapp.domain.invoices.InvoiceRecipient
import com.connectapp.domain.invoices.InvoiceRepository
import com.connectapp.domain.invoices.InvoiceSearchCriterion
import com.connectapp.domain.invoices.InvoiceSummary
import com.connectapp.domain.invoices.InvoiceVoiding
import com.connectapp.domain.invoices.NewInvoice
import com.connectapp.domain.invoices.PaymentMethod
import com.connectapp.domain.invoices.VatTreatment
import com.connectapp.domain.shared.money.Money
import com.connectapp.domain.shared.patient.PatientOption
import com.connectapp.domain.shared.professional.ProfessionalCollege
import com.connectapp.domain.shared.validation.IdentityDocumentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

internal val pepe = PatientOption("pat-1", "Pepe", "Ruiz", IdentityDocumentType.NIF_NIE, "X1234567L")

internal fun sampleInvoice(id: String = "inv-1", authorId: String = "pro-ana") = Invoice(
    id = id,
    number = "2026-000001",
    issuedAtEpochMillis = 1_700_000_000_000L,
    issuer = InvoiceIssuer(authorId, "Ana", "Gil", "12345678Z", ProfessionalCollege.MADRID, "1234"),
    recipient = InvoiceRecipient(pepe.id, "Pepe", "Ruiz", IdentityDocumentType.NIF_NIE, "X1234567L"),
    concept = "Sesión de fisioterapia",
    amount = Money(4550),
    vatTreatment = VatTreatment.EXEMPT_HEALTHCARE,
    paid = false,
    paymentMethod = null,
    voiding = null,
    updatedAtEpochMillis = 1_700_000_000_000L,
)

/** In-memory [InvoiceRepository] with the author rules of D5/D6. */
internal class FakeInvoiceRepository : InvoiceRepository {
    val invoices = MutableStateFlow<Map<String, Invoice>>(emptyMap())
    var lastCreate: NewInvoice? = null
        private set
    var lastSearch: Pair<InvoiceSearchCriterion, String>? = null
        private set

    fun add(id: String, authorId: String) {
        invoices.value += id to sampleInvoice(id, authorId)
    }

    override suspend fun create(authorId: String, invoice: NewInvoice): Result<String> {
        lastCreate = invoice
        val id = "inv-${invoices.value.size + 1}"
        add(id, authorId)
        return Result.success(id)
    }

    override suspend fun setPaid(id: String, authorId: String, paid: Boolean, paymentMethod: PaymentMethod?): Result<Unit> =
        change(id, authorId) { it.copy(paid = paid, paymentMethod = paymentMethod) }

    override suspend fun void(id: String, authorId: String, reason: String): Result<Unit> =
        change(id, authorId) { it.copy(voiding = InvoiceVoiding(reason, 2)) }

    private fun change(id: String, authorId: String, transform: (Invoice) -> Invoice): Result<Unit> {
        val current = invoices.value[id] ?: return Result.failure(InvoiceError.NotFound)
        if (current.issuer.professionalId != authorId) return Result.failure(InvoiceError.NotAuthor)
        if (current.isVoided) return Result.failure(InvoiceError.Voided)
        invoices.value += id to transform(current)
        return Result.success(Unit)
    }

    override fun observe(id: String): Flow<Invoice?> = invoices.map { it[id] }

    override fun search(criterion: InvoiceSearchCriterion, query: String): Flow<List<InvoiceSummary>> {
        lastSearch = criterion to query
        return invoices.map { all ->
            all.values.map {
                InvoiceSummary(it.id, it.number, 0, "Pepe Ruiz", "X1234567L", "Ana Gil", it.amount, it.paid, it.isVoided)
            }
        }
    }

    override suspend fun suggestPatients(query: String): List<PatientOption> =
        listOf(pepe).filter { it.firstName.contains(query, ignoreCase = true) }
}
