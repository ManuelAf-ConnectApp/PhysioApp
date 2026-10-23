package com.connectapp.presentation.invoices

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import com.connectapp.commonresources.badge_unpaid
import com.connectapp.commonresources.button_cancel
import com.connectapp.commonresources.button_export_pdf
import com.connectapp.commonresources.button_mark_paid
import com.connectapp.commonresources.button_save
import com.connectapp.commonresources.button_save_and_export
import com.connectapp.commonresources.export_pdf_warning
import com.connectapp.commonresources.invoice_not_fiscal_notice
import com.connectapp.commonresources.label_amount
import com.connectapp.commonresources.label_paid
import com.connectapp.commonresources.label_patient
import com.connectapp.core.uitest.Texts
import com.connectapp.core.uitest.UiTest
import com.connectapp.core.uitest.clickable
import com.connectapp.core.uitest.setContentWithKoin
import com.connectapp.core.uitest.textField
import com.connectapp.core.uitest.waitForText
import com.connectapp.domain.di.authDomainModule
import com.connectapp.domain.invoices.InvoiceRepository
import com.connectapp.domain.invoices.di.invoicesDomainModule
import com.connectapp.domain.model.ProfessionalProfile
import com.connectapp.domain.repository.AuthRepository
import com.connectapp.domain.shared.money.Money
import com.connectapp.domain.shared.professional.ProfessionalCollege
import com.connectapp.presentation.invoices.detail.InvoiceDetailScreen
import com.connectapp.presentation.invoices.di.invoicesModule
import com.connectapp.presentation.invoices.fake.FakeAuthRepository
import com.connectapp.presentation.invoices.form.InvoiceFormScreen
import org.koin.dsl.module
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Payment receipts (D6) — saving, the paid indicator and the PDF export
 * warning (D19). Real Koin wiring; only the repositories are fakes.
 */
@OptIn(ExperimentalTestApi::class)
class InvoiceScreensUiTest : UiTest() {

    private val auth = FakeAuthRepository().apply {
        professional.value = ProfessionalProfile("pro-ana", "12345678Z", ProfessionalCollege.MADRID, "1", "Ana", "Gil")
    }
    private val invoices = FakeInvoiceRepository()
    private val text = Texts()
    private val modules = arrayOf(
        module {
            single<InvoiceRepository> { invoices }
            single<AuthRepository> { auth }
        },
        authDomainModule,
        invoicesDomainModule,
        invoicesModule,
    )

    @Test
    fun `a receipt needs patient and amount and is saved as not fiscal`() = runComposeUiTest {
        var saved: Pair<String, Boolean>? = null
        setContentWithKoin(*modules) {
            text.resolve(label_patient, label_amount, button_save, button_save_and_export, invoice_not_fiscal_notice)
            InvoiceFormScreen(onSaved = { id, export -> saved = id to export })
        }
        val save = clickable(text[button_save])

        onNodeWithText(text[invoice_not_fiscal_notice]).assertExists()
        save.performScrollTo().assertIsNotEnabled()
        textField(text[label_patient]).performTextInput("Pe")
        waitForText("Pepe Ruiz").performClick()
        textField(text[label_amount]).performTextInput("45,50")
        save.performScrollTo().assertIsEnabled()
        clickable(text[button_save_and_export]).assertIsEnabled()
        save.performClick()
        waitForIdle()

        assertEquals("inv-1" to false, saved)
        assertEquals(Money(4550), invoices.lastCreate?.amount)
        assertEquals(false, invoices.lastCreate?.paid)
    }

    @Test
    fun `the author marks a receipt as paid and the indicator changes`() = runComposeUiTest {
        invoices.add("inv-1", authorId = "pro-ana")
        setContentWithKoin(*modules) {
            text.resolve(badge_unpaid, button_mark_paid, label_paid)
            InvoiceDetailScreen(invoiceId = "inv-1", exportOnOpen = false)
        }
        waitForIdle()

        onNodeWithText(text[badge_unpaid]).assertExists()
        clickable(text[button_mark_paid]).performScrollTo().performClick()
        waitForIdle()
        onNode(hasClickAction() and hasText(text[button_mark_paid]) and hasAnyAncestor(isDialog())).performClick()
        waitForIdle()

        assertTrue(invoices.invoices.value.getValue("inv-1").paid)
        onNodeWithText(text[badge_unpaid]).assertDoesNotExist()
        onNodeWithText(text[label_paid]).assertExists()
    }

    // D19: before a PDF leaves the app, the dialog warns that it is no longer protected.
    @Test
    fun `exporting warns that the PDF leaves the app protection`() = runComposeUiTest {
        invoices.add("inv-1", authorId = "pro-luis")
        setContentWithKoin(*modules) {
            text.resolve(button_export_pdf, export_pdf_warning, button_cancel)
            InvoiceDetailScreen(invoiceId = "inv-1", exportOnOpen = false)
        }
        waitForIdle()

        clickable(text[button_export_pdf]).performScrollTo().performClick()
        waitForIdle()

        onNodeWithText(text[export_pdf_warning]).assertExists()
        clickable(text[button_cancel]).performClick()
        waitForIdle()
        onNodeWithText(text[export_pdf_warning]).assertDoesNotExist()
    }
}
