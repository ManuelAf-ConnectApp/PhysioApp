package com.connectapp.domain.invoices

import com.connectapp.domain.invoices.fake.FakeAuthRepository
import com.connectapp.domain.model.ProfessionalProfile
import com.connectapp.domain.shared.money.Money
import com.connectapp.domain.shared.patient.PatientOption
import com.connectapp.domain.shared.professional.ProfessionalCollege
import com.connectapp.domain.usecase.CurrentAuthorUseCase
import com.connectapp.domain.usecase.ObserveCurrentProfessionalUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class InvoiceUseCasesTest {

    private val auth = FakeAuthRepository()
    private val invoices = RecordingRepository()
    private val author = CurrentAuthorUseCase(ObserveCurrentProfessionalUseCase(auth))
    private val create = CreateInvoiceUseCase(invoices, author)

    private fun signIn() {
        auth.professional.value = ProfessionalProfile("pro-ana", "12345678Z", ProfessionalCollege.MADRID, "1", "Ana", "Gil")
    }

    @Test
    fun `a receipt is issued by the professional of the session with an exact amount`() = runTest {
        signIn()

        create("pat-1", " Sesión ", "45,5", paid = true, paymentMethod = PaymentMethod.CARD)

        assertEquals("pro-ana", invoices.lastAuthor)
        assertEquals(NewInvoice("pat-1", "Sesión", Money(4550), true, PaymentMethod.CARD), invoices.lastCreate)
    }

    @Test
    fun `an unpaid receipt keeps no payment method`() = runTest {
        signIn()

        create("pat-1", "Sesión", "45", paid = false, paymentMethod = PaymentMethod.CASH)

        assertNull(invoices.lastCreate?.paymentMethod)
    }

    @Test
    fun `patient concept and a positive amount are required`() = runTest {
        signIn()

        listOf(
            create(null, "Sesión", "45"),
            create("pat-1", " ", "45"),
            create("pat-1", "x".repeat(InvoiceValidation.MAX_CONCEPT_LENGTH + 1), "45"),
            create("pat-1", "Sesión", "0"),
            create("pat-1", "Sesión", "-3"),
            create("pat-1", "Sesión", "45,555"),
            create("pat-1", "Sesión", "cuarenta"),
        ).forEach { assertEquals(InvoiceError.InvalidInput, it.exceptionOrNull()) }
        assertNull(invoices.lastCreate)
    }

    @Test
    fun `without a professional in session nothing is written`() = runTest {
        assertEquals(InvoiceError.NotAuthenticated, create("pat-1", "Sesión", "45").exceptionOrNull())
        assertNull(invoices.lastCreate)
    }

    @Test
    fun `voiding needs a reason and is done as the session author`() = runTest {
        signIn()
        val void = VoidInvoiceUseCase(invoices, author)

        assertEquals(InvoiceError.InvalidInput, void("inv-1", "  ").exceptionOrNull())
        void("inv-1", " Importe equivocado ")

        assertEquals(listOf("inv-1", "pro-ana", "Importe equivocado"), invoices.lastVoid)
    }

    @Test
    fun `marking as unpaid drops the payment method`() = runTest {
        signIn()

        SetInvoicePaidUseCase(invoices, author)("inv-1", paid = false, paymentMethod = PaymentMethod.CASH)

        assertEquals(Triple("pro-ana", false, null), invoices.lastSetPaid)
    }

    @Test
    fun `amounts parse with comma or dot and two decimals at most`() {
        assertEquals(Money(4550), InvoiceValidation.parseAmount("45,50"))
        assertEquals(Money(4550), InvoiceValidation.parseAmount("45.5"))
        assertNull(InvoiceValidation.parseAmount("0,00"))
        assertTrue(InvoiceValidation.isValidVoidReason("Duplicado"))
        assertFalse(InvoiceValidation.isValidVoidReason(""))
    }

    private suspend operator fun CreateInvoiceUseCase.invoke(patientId: String?, concept: String, amount: String) =
        this(patientId, concept, amount, paid = false, paymentMethod = null)

    private class RecordingRepository : InvoiceRepository {
        var lastAuthor: String? = null
        var lastCreate: NewInvoice? = null
        var lastVoid: List<String>? = null
        var lastSetPaid: Triple<String, Boolean, PaymentMethod?>? = null

        override suspend fun create(authorId: String, invoice: NewInvoice): Result<String> {
            lastAuthor = authorId
            lastCreate = invoice
            return Result.success("inv-1")
        }

        override suspend fun setPaid(id: String, authorId: String, paid: Boolean, paymentMethod: PaymentMethod?): Result<Unit> {
            lastSetPaid = Triple(authorId, paid, paymentMethod)
            return Result.success(Unit)
        }

        override suspend fun void(id: String, authorId: String, reason: String): Result<Unit> {
            lastVoid = listOf(id, authorId, reason)
            return Result.success(Unit)
        }

        override fun observe(id: String): Flow<Invoice?> = emptyFlow()
        override fun search(criterion: InvoiceSearchCriterion, query: String): Flow<List<InvoiceSummary>> = emptyFlow()
        override suspend fun suggestPatients(query: String): List<PatientOption> = emptyList()
    }
}
