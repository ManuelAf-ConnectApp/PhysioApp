package com.connectapp.core.pdf

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PdfDocumentBuilderTest {

    private fun ByteArray.latin1() = map { (it.toInt() and 0xFF).toChar() }.joinToString("")

    private fun sample(paragraphs: Int = 1) = PdfDocumentBuilder(title = "Justificante 2026-000001", footer = "PhysioApp")
        .heading("Justificante de pago")
        .notice("Este documento no es una factura")
        .field("Paciente", "Íñigo López · X1234567L")
        .apply { repeat(paragraphs) { paragraph("Sesión de fisioterapia (45,50 €)") } }
        .build()

    @Test
    fun `the file is a PDF 1_4 with a trailer`() {
        val text = sample().latin1()

        assertTrue(text.startsWith("%PDF-1.4\n"))
        assertTrue(text.endsWith("%%EOF\n"))
        assertTrue("/BaseFont /Helvetica /Encoding /WinAnsiEncoding" in text)
    }

    // A wrong offset makes some readers rebuild the file or refuse to open it.
    @Test
    fun `every cross-reference offset points at its object`() {
        val bytes = sample(paragraphs = 120)
        val text = bytes.latin1()
        val xref = text.substringAfter("startxref\n").substringBefore("\n").toInt()
        assertTrue(text.startsWith("xref\n", xref))

        val lines = text.substring(xref).lines()
        val count = lines[1].substringAfter(' ').toInt()
        (1 until count).forEach { number ->
            val entry = lines[2 + number]
            assertEquals(20, entry.length + 1, "entry $number")
            val offset = entry.take(10).toInt()
            assertTrue(text.startsWith("$number 0 obj\n", offset), "object $number at $offset")
        }
    }

    @Test
    fun `stream lengths match their content`() {
        val text = sample().latin1()
        Regex("<< /Length (\\d+) >>\nstream\n").findAll(text).forEach { match ->
            val start = match.range.last + 1
            val length = match.groupValues[1].toInt()
            assertTrue(text.startsWith("\nendstream", start + length))
        }
    }

    @Test
    fun `long content flows onto numbered pages`() {
        val text = sample(paragraphs = 120).latin1()
        val pages = Regex("/Count (\\d+)").find(text)!!.groupValues[1].toInt()

        assertTrue(pages > 1)
        assertTrue("($pages / $pages) Tj" in text)
    }

    @Test
    fun `Spanish text is written in WinAnsi`() {
        val text = sample().latin1()

        // Í = 315, ñ = 361, ó = 363 (octal); € = 0x80 = 200; the middle dot = 267.
        assertTrue("(\\315\\361igo L\\363pez \\267 X1234567L) Tj" in text)
        assertTrue("(45,50 \\200\\)) Tj" in text)
    }

    @Test
    fun `letters missing from WinAnsi lose their diacritic`() {
        assertEquals("Stefanescu Lukasz \\277?", PdfWriter.encodeText("Ștefănescu Łukasz ¿漢"))
        assertEquals(WinAnsi.FALLBACK_FROM.length, WinAnsi.FALLBACK_TO.length)
    }

    @Test
    fun `lines wrap on spaces and keep explicit breaks`() {
        val width = textWidth("Lumbalgia mecánica", 11f, bold = false)

        assertEquals(
            listOf("Lumbalgia mecánica", "crónica", "", "Revisión"),
            wrap("Lumbalgia mecánica crónica\n\nRevisión", 11f, bold = false, maxWidth = width + 1f),
        )
    }

    @Test
    fun `a word wider than the line is split`() {
        val lines = wrap("A".repeat(200), 11f, bold = false, maxWidth = 100f)

        assertTrue(lines.size > 1)
        assertTrue(lines.all { textWidth(it, 11f, bold = false) <= 100f })
        assertEquals("A".repeat(200), lines.joinToString(""))
    }

    @Test
    fun `widths follow the Helvetica metrics`() {
        // "Ana" = 667 + 556 + 556 in regular; bold A and n are wider.
        assertEquals(1779 * 10 / 1000f, textWidth("Ana", 10f, bold = false))
        assertEquals((722 + 611 + 556) * 10 / 1000f, textWidth("Ana", 10f, bold = true))
        assertEquals(textWidth("i", 10f, bold = false) + 0.56f, textWidth("í", 10f, bold = false), 0.001f)
    }

    @Test
    fun `numbers never depend on the device locale`() {
        assertEquals("595.28", num(595.28f))
        assertEquals("56", num(56f))
        assertEquals("0.4", num(0.4f))
        assertEquals("-3.05", num(-3.05f))
    }
}
