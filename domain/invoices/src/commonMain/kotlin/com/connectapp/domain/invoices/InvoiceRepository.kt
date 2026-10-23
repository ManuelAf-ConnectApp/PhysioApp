package com.connectapp.domain.invoices

import com.connectapp.domain.shared.patient.PatientOption
import kotlinx.coroutines.flow.Flow

/**
 * Payment receipts in the local database (D6). The author is always passed in
 * by the use cases from the session; the repository checks it on every change. Receipts are never
 * deleted. Failures are always [InvoiceError]s.
 */
interface InvoiceRepository {
    /** Issues the receipt with the next number of the current year's series; returns its id. */
    suspend fun create(authorId: String, invoice: NewInvoice): Result<String>

    /** Collection status. Only for the author of a receipt that is not voided. */
    suspend fun setPaid(id: String, authorId: String, paid: Boolean, paymentMethod: PaymentMethod?): Result<Unit>

    /** Only for the author of a receipt that is not voided. */
    suspend fun void(id: String, authorId: String, reason: String): Result<Unit>

    fun observe(id: String): Flow<Invoice?>

    fun search(criterion: InvoiceSearchCriterion, query: String): Flow<List<InvoiceSummary>>

    /** Active patients whose name or document matches [query], for the patient picker. */
    suspend fun suggestPatients(query: String): List<PatientOption>
}
