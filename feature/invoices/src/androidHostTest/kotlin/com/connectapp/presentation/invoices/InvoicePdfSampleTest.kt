package com.connectapp.presentation.invoices

import com.connectapp.domain.invoices.InvoiceVoiding
import com.connectapp.domain.invoices.PaymentMethod
import com.connectapp.presentation.invoices.pdf.buildInvoicePdf
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

/** Writes sample receipts to build/pdf-samples/ to check the layout by eye in any PDF reader. */
class InvoicePdfSampleTest {

    @Test
    fun `writes sample receipts`() {
        val folder = File("build/pdf-samples").apply { mkdirs() }
        val paid = sampleInvoice().copy(paid = true, paymentMethod = PaymentMethod.CARD)
        File(folder, "receipt.pdf").writeBytes(buildInvoicePdf(paid, spanishTexts.copy(status = "Pagado · Tarjeta")))
        val voided = sampleInvoice().copy(voiding = InvoiceVoiding("Importe equivocado", 0))
        val voidedTexts = spanishTexts.copy(voidedNotice = "Anulado el 15/11/2023 10:00. Motivo: Importe equivocado")
        File(folder, "receipt-voided.pdf").writeBytes(buildInvoicePdf(voided, voidedTexts))

        assertTrue(File(folder, "receipt.pdf").length() > 0)
    }
}
