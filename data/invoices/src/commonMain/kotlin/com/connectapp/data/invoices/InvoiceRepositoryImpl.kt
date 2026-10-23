package com.connectapp.data.invoices

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.connectapp.core.database.SearchByNumber
import com.connectapp.core.database.physio.PhysioDatabase
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
import com.connectapp.domain.shared.text.SearchText
import com.connectapp.domain.shared.validation.IdentityDocumentType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import com.connectapp.core.database.Invoice as InvoiceRow

/**
 * Payment receipts over the encrypted [PhysioDatabase] (phase 9; D6).
 *
 * - The number is the next one of the issue year's series (`2026-000001`), read, used and
 *   incremented in the same transaction as the insert, so numbers never repeat or leave gaps.
 * - The author's and the patient's data are copied onto the receipt when it is issued.
 * - Only the author changes the collection status or voids a receipt that is not voided yet.
 */
class InvoiceRepositoryImpl(
    private val database: PhysioDatabase,
    private val now: () -> Long,
    private val seriesOf: (epochMillis: Long) -> String = ::yearSeries,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : InvoiceRepository {

    private val invoices = database.invoiceQueries
    private val patients = database.patientQueries
    private val professionals = database.professionalQueries

    override suspend fun create(authorId: String, invoice: NewInvoice): Result<String> = operation {
        val patient = patients.selectPatientById(invoice.patientId).executeAsOneOrNull()
        if (patient == null || patient.archived_at != null) throw InvoiceError.PatientNotAvailable
        val issuer = professionals.selectProfessionalById(authorId).executeAsOneOrNull()
            ?: throw InvoiceError.NotAuthenticated
        val timestamp = now()
        val series = seriesOf(timestamp)
        invoices.insertSeriesIfMissing(series, series)
        val number = invoices.selectSeriesNextNumber(series).executeAsOne()
        val id = randomUuid()
        invoices.insertInvoice(
            id = id,
            series_id = series,
            number = number,
            display_number = displayNumber(series, number),
            professional_id = authorId,
            patient_id = patient.id,
            issuer_first_name = issuer.first_name,
            issuer_last_name = issuer.last_name,
            issuer_nif = issuer.nif,
            issuer_college_id = issuer.college_id,
            issuer_license_number = issuer.license_number,
            recipient_first_name = patient.first_name,
            recipient_last_name = patient.last_name,
            recipient_document_type = patient.document_type,
            recipient_document_number = patient.document_number,
            concept = invoice.concept,
            amount_cents = invoice.amount.cents,
            currency = invoice.amount.currency.code,
            paid = invoice.paid.toLong(),
            payment_method = invoice.paymentMethod?.takeIf { invoice.paid }?.name,
            issued_at = timestamp,
            created_at = timestamp,
            updated_at = timestamp,
        )
        invoices.incrementSeriesNumber(series)
        id
    }

    override suspend fun setPaid(
        id: String,
        authorId: String,
        paid: Boolean,
        paymentMethod: PaymentMethod?,
    ): Result<Unit> = operation {
        editableInvoice(id, authorId)
        invoices.updatePaid(paid.toLong(), paymentMethod?.takeIf { paid }?.name, now(), id)
    }

    override suspend fun void(id: String, authorId: String, reason: String): Result<Unit> = operation {
        editableInvoice(id, authorId)
        val timestamp = now()
        invoices.voidInvoice(voided_at = timestamp, void_reason = reason, updated_at = timestamp, id = id)
    }

    override fun observe(id: String): Flow<Invoice?> =
        invoices.selectInvoiceById(id).asFlow().mapToOneOrNull(dispatcher).map { it?.toInvoice() }

    override fun search(criterion: InvoiceSearchCriterion, query: String): Flow<List<InvoiceSummary>> {
        // The three searches select the same columns; SQLDelight maps them all to SearchByNumber.
        val search = when (criterion) {
            InvoiceSearchCriterion.NUMBER ->
                invoices.searchByNumber(contains(query.filterNot { it.isWhitespace() }), SEARCH_LIMIT)
            InvoiceSearchCriterion.PATIENT_NAME ->
                invoices.searchByPatientName(contains(SearchText.normalize(query)), SEARCH_LIMIT, ::SearchByNumber)
            InvoiceSearchCriterion.PATIENT_DOCUMENT ->
                invoices.searchByPatientDocument(documentPattern(query), SEARCH_LIMIT, ::SearchByNumber)
        }
        return search.asFlow().mapToList(dispatcher).map { rows -> rows.map { it.toSummary() } }
    }

    override suspend fun suggestPatients(query: String): List<PatientOption> = withContext(dispatcher) {
        patients.suggestActivePatients(
            name_pattern = contains(SearchText.normalize(query)),
            document_pattern = documentPattern(query),
            limit = SUGGESTION_LIMIT,
        ).executeAsList().map {
            PatientOption(
                id = it.id,
                firstName = it.first_name,
                lastName = it.last_name,
                documentType = IdentityDocumentType.valueOf(it.document_type),
                documentNumber = it.document_number,
            )
        }
    }

    /** The receipt, when [authorId] issued it and it is not voided (D5, D6). */
    private fun editableInvoice(id: String, authorId: String) =
        (invoices.selectInvoiceById(id).executeAsOneOrNull() ?: throw InvoiceError.NotFound).also {
            if (it.professional_id != authorId) throw InvoiceError.NotAuthor
            if (it.voided_at != null) throw InvoiceError.Voided
        }

    /** Runs [block] in a transaction on [dispatcher]; [InvoiceError]s become failures. */
    private suspend fun <T> operation(block: () -> T): Result<T> = withContext(dispatcher) {
        try {
            Result.success(database.transactionWithResult { block() })
        } catch (error: CancellationException) {
            throw error
        } catch (error: InvoiceError) {
            Result.failure(error)
        } catch (@Suppress("TooGenericExceptionCaught") error: Exception) {
            Result.failure(InvoiceError.Unknown(error))
        }
    }

    private companion object {
        const val SEARCH_LIMIT = 200L
        const val SUGGESTION_LIMIT = 10L
    }
}

private const val NUMBER_DIGITS = 6

/** `2026-000001`: the series and the number within it, zero-padded so that they sort as text. */
internal fun displayNumber(series: String, number: Long) = "$series-${number.toString().padStart(NUMBER_DIGITS, '0')}"

/** The series of a receipt is the year it is issued in, in the device's time zone. */
@OptIn(ExperimentalTime::class)
internal fun yearSeries(epochMillis: Long): String =
    Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(TimeZone.currentSystemDefault()).year.toString()

private fun InvoiceRow.toInvoice() = Invoice(
    id = id,
    number = display_number,
    issuedAtEpochMillis = issued_at,
    issuer = InvoiceIssuer(
        professionalId = professional_id,
        firstName = issuer_first_name,
        lastName = issuer_last_name,
        nif = issuer_nif,
        college = ProfessionalCollege.fromId(issuer_college_id),
        licenseNumber = issuer_license_number,
    ),
    recipient = InvoiceRecipient(
        patientId = patient_id,
        firstName = recipient_first_name,
        lastName = recipient_last_name,
        documentType = IdentityDocumentType.valueOf(recipient_document_type),
        documentNumber = recipient_document_number,
    ),
    concept = concept,
    amount = money(amount_cents, currency),
    vatTreatment = VatTreatment.valueOf(vat_treatment),
    paid = paid == 1L,
    paymentMethod = payment_method?.let { method -> PaymentMethod.entries.firstOrNull { it.name == method } },
    voiding = voided_at?.let { InvoiceVoiding(void_reason.orEmpty(), it) },
    updatedAtEpochMillis = updated_at,
)

private fun SearchByNumber.toSummary() = InvoiceSummary(
    id = id,
    number = display_number,
    issuedAtEpochMillis = issued_at,
    patientName = "$recipient_first_name $recipient_last_name",
    patientDocument = recipient_document_number,
    professionalName = "$issuer_first_name $issuer_last_name",
    amount = money(amount_cents, currency),
    paid = paid == 1L,
    isVoided = voided_at != null,
)

private fun money(cents: Long, currency: String) =
    Money(cents, Money.Currency.entries.firstOrNull { it.code == currency } ?: Money.Currency.EUR)

private fun Boolean.toLong() = if (this) 1L else 0L

/** LIKE patterns with `\` escaping the wildcards the user may type (see Invoice.sq). */
private fun escapeLike(value: String) = value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")
private fun contains(value: String) = "%" + escapeLike(value) + "%"

/** Document keys are upper-case without spaces for every document type. */
private fun documentPattern(query: String) = contains(query.filterNot { it.isWhitespace() }.uppercase())

@OptIn(ExperimentalUuidApi::class)
private fun randomUuid(): String = Uuid.random().toString()
