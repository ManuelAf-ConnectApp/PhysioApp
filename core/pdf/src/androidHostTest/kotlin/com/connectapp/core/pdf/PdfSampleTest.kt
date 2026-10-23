package com.connectapp.core.pdf

import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Writes a two-page sample to build/pdf-samples/ so the layout can be opened and checked by eye in
 * any PDF reader; the assertions only guard that the file was produced.
 */
class PdfSampleTest {

    @Test
    fun `writes a sample document`() {
        val builder = PdfDocumentBuilder(
            title = "Muestra",
            footer = "PhysioApp · Documento generado el 20/10/2026 14:05",
            pageLabel = { page, count -> "Página $page de $count" },
        )
            .heading("Justificante de pago 2026-000001")
            .notice("Este documento no es una factura")
            .field("Fecha", "20/10/2026 14:05")
            .field("Profesional", "Aina García Pérez · NIF 12345678Z · Colegio de Fisioterapeutas de Madrid · Nº 1234")
            .field("Paciente", "Íñigo López Núñez · NIF/NIE X1234567L")
            .field("Concepto", "Sesión de fisioterapia")
            .field("Importe", "1.234,50 €")
            .note("Operación exenta de IVA (art. 20.Uno.3º de la Ley 37/1992 del IVA).")
            .rule()
            .subheading("Texto largo")
        repeat(40) { builder.paragraph("Línea $it: ${"Lumbalgia mecánica crónica con irradiación ".repeat(3)}") }
        val file = File("build/pdf-samples/sample.pdf").apply { parentFile.mkdirs() }
        file.writeBytes(builder.build())

        assertTrue(file.length() > 0)
    }
}
