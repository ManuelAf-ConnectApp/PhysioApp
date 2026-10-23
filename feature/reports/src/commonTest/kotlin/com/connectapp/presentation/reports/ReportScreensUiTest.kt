package com.connectapp.presentation.reports

import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import com.connectapp.commonresources.button_annul
import com.connectapp.commonresources.button_edit
import com.connectapp.commonresources.button_export_pdf
import com.connectapp.commonresources.button_save
import com.connectapp.commonresources.label_condition_text
import com.connectapp.commonresources.label_patient
import com.connectapp.commonresources.label_report_text
import com.connectapp.core.uitest.Texts
import com.connectapp.core.uitest.UiTest
import com.connectapp.core.uitest.clickable
import com.connectapp.core.uitest.setContentWithKoin
import com.connectapp.core.uitest.textField
import com.connectapp.core.uitest.waitForText
import com.connectapp.domain.di.authDomainModule
import com.connectapp.domain.model.ProfessionalProfile
import com.connectapp.domain.reports.ReportRepository
import com.connectapp.domain.reports.di.reportsDomainModule
import com.connectapp.domain.repository.AuthRepository
import com.connectapp.domain.shared.professional.ProfessionalCollege
import com.connectapp.presentation.reports.detail.ReportDetailScreen
import com.connectapp.presentation.reports.di.reportsModule
import com.connectapp.presentation.reports.fake.FakeAuthRepository
import com.connectapp.presentation.reports.form.ReportFormScreen
import org.koin.dsl.module
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * A report is created picking the patient from suggestions, and only its
 * author may edit or annul it (D17). Real Koin wiring; only the repositories are fakes.
 */
@OptIn(ExperimentalTestApi::class)
class ReportScreensUiTest : UiTest() {

    private val auth = FakeAuthRepository().apply {
        professional.value = ProfessionalProfile("pro-ana", "12345678Z", ProfessionalCollege.MADRID, "1", "Ana", "Gil")
    }
    private val reports = FakeReportRepository()
    private val text = Texts()
    private val modules = arrayOf(
        module {
            single<ReportRepository> { reports }
            single<AuthRepository> { auth }
        },
        authDomainModule,
        reportsDomainModule,
        reportsModule,
    )

    @Test
    fun `a report is saved for the patient picked from the suggestions`() = runComposeUiTest {
        var savedId: String? = null
        setContentWithKoin(*modules) {
            text.resolve(label_patient, label_report_text, label_condition_text, button_save)
            ReportFormScreen(reportId = null, onSaved = { savedId = it })
        }
        val save = clickable(text[button_save])

        textField(text[label_patient]).performTextInput("Pe")
        waitForText("Pepe Ruiz").performClick()
        waitForIdle()
        save.performScrollTo().assertIsNotEnabled()
        textField(text[label_report_text]).performTextInput("Dolor lumbar de tres semanas")
        textField(text[label_condition_text]).performTextInput("Lumbalgia mecánica")
        save.performScrollTo().assertIsEnabled()
        save.performClick()
        waitForIdle()

        assertEquals("rep-1", savedId)
        assertEquals("pro-ana", reports.reports.value.getValue("rep-1").professional.id)
    }

    @Test
    fun `a report shows edit and annul only to its author`() = runComposeUiTest {
        reports.add("rep-mine", authorId = "pro-ana")
        reports.add("rep-other", authorId = "pro-luis")
        var reportId by mutableStateOf("rep-mine")
        setContentWithKoin(*modules) {
            text.resolve(button_edit, button_annul, button_export_pdf)
            key(reportId) {
                ReportDetailScreen(reportId = reportId, onEdit = {})
            }
        }
        waitForIdle()

        clickable(text[button_edit]).assertExists()
        clickable(text[button_annul]).assertExists()
        clickable(text[button_export_pdf]).assertExists()

        reportId = "rep-other"
        waitForIdle()

        clickable(text[button_export_pdf]).assertExists()
        clickable(text[button_edit]).assertDoesNotExist()
        clickable(text[button_annul]).assertDoesNotExist()
    }
}
