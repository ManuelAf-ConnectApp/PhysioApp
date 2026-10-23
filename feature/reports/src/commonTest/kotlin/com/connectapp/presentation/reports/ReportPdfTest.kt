package com.connectapp.presentation.reports

import com.connectapp.commonresources.export.PdfPageTexts
import com.connectapp.domain.reports.ClinicalReport
import com.connectapp.domain.reports.ReportAnnulment
import com.connectapp.domain.reports.ReportPatient
import com.connectapp.domain.reports.ReportProfessional
import com.connectapp.presentation.reports.pdf.ReportPdfTexts
import com.connectapp.presentation.reports.pdf.buildReportPdf
import kotlin.test.Test
import kotlin.test.assertTrue

internal val sampleReport = ClinicalReport(
    id = "rep-1",
    code = "INF-AAAA2222",
    patient = ReportPatient(pepe.id, pepe.firstName, pepe.lastName, pepe.documentType, pepe.documentNumber),
    professional = ReportProfessional("pro-ana", "Ana", "Gil"),
    reportText = "Dolor lumbar de tres semanas.\nMejora con ejercicio.",
    conditionText = "Lumbalgia mecánica",
    createdAtEpochMillis = 0,
    updatedAtEpochMillis = 0,
    annulment = null,
    versionCount = 0,
)

internal val reportTexts = ReportPdfTexts(
    heading = "Informe clínico INF-AAAA2222",
    confidential = "Confidencial: contiene datos de salud",
    annulledNotice = null,
    dates = "Creado el 14/11/2023 23:13",
    patientLabel = "Paciente",
    patient = "Pepe Ruiz\nNIF/NIE X1234567L",
    professionalLabel = "Profesional",
    reportLabel = "Datos del informe",
    conditionLabel = "Dolencia del paciente",
    page = PdfPageTexts(footer = "Generado con PhysioApp el 20/10/2026 14:05", pageTemplate = "Página %1\$d de %2\$d"),
)

class ReportPdfTest {

    private fun ByteArray.latin1() = map { (it.toInt() and 0xFF).toChar() }.joinToString("")

    @Test
    fun `the report PDF is confidential and keeps the line breaks of the text`() {
        val text = buildReportPdf(sampleReport, reportTexts).latin1()

        assertTrue("(Confidencial: contiene datos de salud) Tj" in text)
        assertTrue("(Dolor lumbar de tres semanas.) Tj" in text)
        assertTrue("(Mejora con ejercicio.) Tj" in text)
        assertTrue("/Title (Informe cl\\355nico INF-AAAA2222)" in text)
    }

    @Test
    fun `an annulled report is marked as such`() {
        val annulled = sampleReport.copy(annulment = ReportAnnulment("Paciente equivocado", 0))
        val texts = reportTexts.copy(annulledNotice = "Anulado el 15/11/2023. Motivo: Paciente equivocado")

        assertTrue("(ANULADO EL 15/11/2023. MOTIVO: PACIENTE EQUIVOCADO) Tj" in buildReportPdf(annulled, texts).latin1())
    }
}
