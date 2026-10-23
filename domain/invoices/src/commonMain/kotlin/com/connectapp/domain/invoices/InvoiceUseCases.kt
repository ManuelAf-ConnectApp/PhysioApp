package com.connectapp.domain.invoices

import com.connectapp.domain.shared.patient.PatientOption
import com.connectapp.domain.usecase.CurrentAuthorUseCase
import kotlinx.coroutines.flow.Flow

/**
 * "Crear factura" (D6): the professional is the one of the session, not
 * editable, and the number is assigned by the repository. A payment method is kept only when paid.
 */
class CreateInvoiceUseCase(
    private val repository: InvoiceRepository,
    private val currentAuthor: CurrentAuthorUseCase,
) {
    suspend operator fun invoke(
        patientId: String?,
        concept: String,
        amountText: String,
        paid: Boolean,
        paymentMethod: PaymentMethod?,
    ): Result<String> {
        val author = currentAuthor()
        val amount = InvoiceValidation.parseAmount(amountText)
        return when {
            InvoiceValidation.errors(patientId, concept, amountText).isNotEmpty() ||
                patientId == null || amount == null -> Result.failure(InvoiceError.InvalidInput)
            author == null -> Result.failure(InvoiceError.NotAuthenticated)
            else -> repository.create(
                author,
                NewInvoice(patientId, concept.trim(), amount, paid, paymentMethod.takeIf { paid }),
            )
        }
    }
}

/** Collection status, the only change after issuing (D6); only the author makes it (D5). */
class SetInvoicePaidUseCase(
    private val repository: InvoiceRepository,
    private val currentAuthor: CurrentAuthorUseCase,
) {
    suspend operator fun invoke(id: String, paid: Boolean, paymentMethod: PaymentMethod?): Result<Unit> {
        val author = currentAuthor() ?: return Result.failure(InvoiceError.NotAuthenticated)
        return repository.setPaid(id, author, paid, paymentMethod.takeIf { paid })
    }
}

/** D6/D8: receipts are never deleted; the author may void one with a reason and it keeps its number. */
class VoidInvoiceUseCase(
    private val repository: InvoiceRepository,
    private val currentAuthor: CurrentAuthorUseCase,
) {
    suspend operator fun invoke(id: String, reason: String): Result<Unit> {
        val author = currentAuthor()
        return when {
            !InvoiceValidation.isValidVoidReason(reason) -> Result.failure(InvoiceError.InvalidInput)
            author == null -> Result.failure(InvoiceError.NotAuthenticated)
            else -> repository.void(id, author, reason.trim())
        }
    }
}

class ObserveInvoiceUseCase(private val repository: InvoiceRepository) {
    operator fun invoke(id: String): Flow<Invoice?> = repository.observe(id)
}

class SearchInvoicesUseCase(private val repository: InvoiceRepository) {
    operator fun invoke(criterion: InvoiceSearchCriterion, query: String): Flow<List<InvoiceSummary>> =
        repository.search(criterion, query.trim())
}

class SuggestInvoicePatientsUseCase(private val repository: InvoiceRepository) {
    suspend operator fun invoke(query: String): List<PatientOption> =
        if (query.isBlank()) emptyList() else repository.suggestPatients(query.trim())
}
