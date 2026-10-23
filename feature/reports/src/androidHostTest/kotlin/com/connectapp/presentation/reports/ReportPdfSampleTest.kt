package com.connectapp.presentation.reports

import com.connectapp.presentation.reports.pdf.buildReportPdf
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

/** Writes a sample report to build/pdf-samples/ to check the layout by eye in any PDF reader. */
class ReportPdfSampleTest {

    @Test
    fun `writes a sample report`() {
        val file = File("build/pdf-samples/report.pdf").apply { parentFile.mkdirs() }
        val long = sampleReport.copy(reportText = List(30) { "Sesión ${it + 1}: movilización y ejercicio terapéutico." }.joinToString("\n"))
        file.writeBytes(buildReportPdf(long, reportTexts))

        assertTrue(file.length() > 0)
    }
}
