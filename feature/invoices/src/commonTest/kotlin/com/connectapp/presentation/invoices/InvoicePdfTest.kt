package com.connectapp.presentation.invoices

import com.connectapp.commonresources.export.PdfPageTexts
import com.connectapp.domain.invoices.InvoiceVoiding
import com.connectapp.presentation.invoices.pdf.InvoicePdfTexts
import com.connectapp.presentation.invoices.pdf.buildInvoicePdf
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal val spanishTexts = InvoicePdfTexts(
    heading = "Justificante de pago 2026-000001",
    notFiscalNotice = "Este documento no es una factura",
    voidedNotice = null,
    dateLabel = "Fecha",
    date = "14/11/2023 23:13",
    issuerLabel = "Emitido por",
    issuer = "Ana Gil\nNIF 12345678Z · Colegio de Fisioterapeutas de Madrid · Nº de colegiado 1234",
    patientLabel = "Paciente",
    patient = "Pepe Ruiz\nNIF/NIE X1234567L",
    conceptLabel = "Concepto",
    amountLabel = "Importe",
    statusLabel = "Estado del cobro",
    status = "Pendiente",
    vatNote = "Operación exenta de IVA (art. 20.Uno.3º de la Ley 37/1992 del IVA).",
    notFiscalExplanation = "Justificante de pago interno.",
    page = PdfPageTexts(footer = "Generado con PhysioApp el 20/10/2026 14:05", pageTemplate = "Página %1\$d de %2\$d"),
)

class InvoicePdfTest {

    private fun ByteArray.latin1() = map { (it.toInt() and 0xFF).toChar() }.joinToString("")

    // D6: the PDF never passes for an invoice.
    @Test
    fun `the receipt PDF says it is not an invoice and shows the exact amount`() {
        val text = buildInvoicePdf(sampleInvoice(), spanishTexts).latin1()

        assertTrue(text.startsWith("%PDF-1.4"))
        assertTrue("(Este documento no es una factura) Tj" in text)
        assertTrue("(45,50 \\200) Tj" in text)
        assertTrue("(P\\341gina 1 de 1) Tj" in text)
        assertFalse("ANULADO" in text)
    }

    @Test
    fun `a voided receipt is marked above everything else`() {
        val voided = sampleInvoice().copy(voiding = InvoiceVoiding("Duplicado", 0))
        val text = buildInvoicePdf(voided, spanishTexts.copy(voidedNotice = "Anulado el 15/11/2023. Motivo: Duplicado")).latin1()

        val voidedAt = text.indexOf("(ANULADO EL 15/11/2023. MOTIVO: DUPLICADO) Tj")
        assertTrue(voidedAt in 0 until text.indexOf("(Este documento no es una factura) Tj"))
    }

    // The file name and document title carry the number only, never personal data.
    @Test
    fun `the document title has no personal data`() {
        val text = buildInvoicePdf(sampleInvoice(), spanishTexts).latin1()

        assertTrue("/Title (Justificante de pago 2026-000001)" in text)
    }
}
