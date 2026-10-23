package com.connectapp.presentation.invoices

import com.connectapp.commonresources.error_invoice_not_author
import com.connectapp.domain.invoices.CreateInvoiceUseCase
import com.connectapp.domain.invoices.InvoiceSearchCriterion
import com.connectapp.domain.invoices.ObserveInvoiceUseCase
import com.connectapp.domain.invoices.PaymentMethod
import com.connectapp.domain.invoices.SearchInvoicesUseCase
import com.connectapp.domain.invoices.SetInvoicePaidUseCase
import com.connectapp.domain.invoices.SuggestInvoicePatientsUseCase
import com.connectapp.domain.invoices.VoidInvoiceUseCase
import com.connectapp.domain.model.ProfessionalProfile
import com.connectapp.domain.shared.money.Money
import com.connectapp.domain.shared.professional.ProfessionalCollege
import com.connectapp.domain.usecase.CurrentAuthorUseCase
import com.connectapp.domain.usecase.ObserveCurrentProfessionalUseCase
import com.connectapp.presentation.invoices.detail.InvoiceDetailIntent
import com.connectapp.presentation.invoices.detail.InvoiceDetailViewModel
import com.connectapp.presentation.invoices.detail.InvoiceDialog
import com.connectapp.presentation.invoices.fake.FakeAuthRepository
import com.connectapp.presentation.invoices.form.InvoiceFormEffect
import com.connectapp.presentation.invoices.form.InvoiceFormIntent
import com.connectapp.presentation.invoices.form.InvoiceFormViewModel
import com.connectapp.presentation.invoices.search.InvoiceSearchIntent
import com.connectapp.presentation.invoices.search.InvoiceSearchViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class InvoiceViewModelsTest {

    private val dispatcher = StandardTestDispatcher()
    private val auth = FakeAuthRepository().apply {
        professional.value = ProfessionalProfile("pro-ana", "12345678Z", ProfessionalCollege.MADRID, "1", "Ana", "Gil")
    }
    private val invoices = FakeInvoiceRepository()
    private val observeProfessional = ObserveCurrentProfessionalUseCase(auth)
    private val author = CurrentAuthorUseCase(observeProfessional)

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun form() = InvoiceFormViewModel(
        CreateInvoiceUseCase(invoices, author),
        SuggestInvoicePatientsUseCase(invoices),
        observeProfessional,
    )

    private fun detail(id: String, exportOnOpen: Boolean = false) = InvoiceDetailViewModel(
        id,
        exportOnOpen,
        ObserveInvoiceUseCase(invoices),
        author,
        SetInvoicePaidUseCase(invoices, author),
        VoidInvoiceUseCase(invoices, author),
    )

    // Session professional, picked patient, exact amount, paid flag.
    @Test
    fun `a new receipt is issued by the session professional and can ask for the PDF`() = runTest {
        val viewModel = form()
        val effects = mutableListOf<InvoiceFormEffect>()
        val job = launch { viewModel.effect.toList(effects) }
        viewModel.onIntent(InvoiceFormIntent.DefaultConceptLoaded("Sesión de fisioterapia"))
        advanceUntilIdle()
        assertEquals("Ana Gil", viewModel.state.value.authorName)
        assertFalse(viewModel.state.value.hasUnsavedChanges)

        viewModel.onIntent(InvoiceFormIntent.PatientQueryChanged("pep"))
        advanceUntilIdle()
        viewModel.onIntent(InvoiceFormIntent.PatientSelected(pepe))
        assertFalse(viewModel.state.value.canSubmit)
        viewModel.onIntent(InvoiceFormIntent.AmountChanged("45,50"))
        viewModel.onIntent(InvoiceFormIntent.PaidChanged(true))
        viewModel.onIntent(InvoiceFormIntent.PaymentMethodChanged(PaymentMethod.CARD))
        assertTrue(viewModel.state.value.hasUnsavedChanges)
        viewModel.onIntent(InvoiceFormIntent.SaveClicked(export = true))
        advanceUntilIdle()

        assertEquals(listOf<InvoiceFormEffect>(InvoiceFormEffect.Saved("inv-1", export = true)), effects)
        val created = invoices.lastCreate
        assertEquals(Money(4550), created?.amount)
        assertEquals("Sesión de fisioterapia", created?.concept)
        assertEquals(PaymentMethod.CARD, created?.paymentMethod)
        assertFalse(viewModel.state.value.hasUnsavedChanges)
        job.cancel()
    }

    @Test
    fun `unticking paid forgets the payment method`() = runTest {
        val viewModel = form()
        viewModel.onIntent(InvoiceFormIntent.PaidChanged(true))
        viewModel.onIntent(InvoiceFormIntent.PaymentMethodChanged(PaymentMethod.CASH))

        viewModel.onIntent(InvoiceFormIntent.PaidChanged(false))

        assertNull(viewModel.state.value.paymentMethod)
    }

    @Test
    fun `a concept typed by the user is not replaced by the default one`() = runTest {
        val viewModel = form()
        viewModel.onIntent(InvoiceFormIntent.ConceptChanged("Bono de 5 sesiones"))

        viewModel.onIntent(InvoiceFormIntent.DefaultConceptLoaded("Sesión de fisioterapia"))

        assertEquals("Bono de 5 sesiones", viewModel.state.value.concept)
    }

    // D5: only the author changes the collection status or voids.
    @Test
    fun `only the author may mark as paid or void and voiding needs a reason`() = runTest {
        invoices.add("mine", authorId = "pro-ana")
        invoices.add("theirs", authorId = "pro-luis")
        val mine = detail("mine")
        val theirs = detail("theirs")
        advanceUntilIdle()
        assertTrue(mine.state.value.canChange)
        assertFalse(theirs.state.value.canChange)

        mine.onIntent(InvoiceDetailIntent.MarkPaidClicked)
        mine.onIntent(InvoiceDetailIntent.PaymentMethodChanged(PaymentMethod.BIZUM))
        mine.onIntent(InvoiceDetailIntent.MarkPaidConfirmed)
        advanceUntilIdle()
        assertEquals(PaymentMethod.BIZUM, mine.state.value.invoice?.paymentMethod)

        mine.onIntent(InvoiceDetailIntent.VoidClicked)
        assertFalse(mine.state.value.canConfirmVoid)
        mine.onIntent(InvoiceDetailIntent.VoidReasonChanged("Importe equivocado"))
        mine.onIntent(InvoiceDetailIntent.VoidConfirmed)
        advanceUntilIdle()
        assertEquals("Importe equivocado", mine.state.value.invoice?.voiding?.reason)
        assertFalse(mine.state.value.canChange)

        theirs.onIntent(InvoiceDetailIntent.MarkUnpaidClicked)
        advanceUntilIdle()
        assertEquals(error_invoice_not_author, theirs.state.value.errorMessage)
    }

    @Test
    fun `save and export opens the export dialog on arrival`() = runTest {
        invoices.add("inv-1", authorId = "pro-ana")

        assertEquals(InvoiceDialog.Export, detail("inv-1", exportOnOpen = true).state.value.dialog)
        assertNull(detail("inv-1").state.value.dialog)
    }

    @Test
    fun `changing the search criterion clears the query`() = runTest {
        val viewModel = InvoiceSearchViewModel(SearchInvoicesUseCase(invoices))
        viewModel.onIntent(InvoiceSearchIntent.QueryChanged("pepe"))

        viewModel.onIntent(InvoiceSearchIntent.CriterionSelected(InvoiceSearchCriterion.NUMBER))
        advanceUntilIdle()

        assertEquals(InvoiceSearchCriterion.NUMBER to "", invoices.lastSearch)
    }
}
