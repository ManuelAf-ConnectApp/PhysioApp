package com.connectapp.presentation.professionals

import androidx.compose.ui.semantics.SemanticsActions
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
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.runComposeUiTest
import com.connectapp.commonresources.button_archive
import com.connectapp.commonresources.button_save
import com.connectapp.commonresources.error_invalid_nif
import com.connectapp.commonresources.label_college
import com.connectapp.commonresources.label_first_name
import com.connectapp.commonresources.label_last_name
import com.connectapp.commonresources.label_license_number
import com.connectapp.commonresources.label_nif
import com.connectapp.core.uitest.Texts
import com.connectapp.core.uitest.UiTest
import com.connectapp.core.uitest.clickable
import com.connectapp.core.uitest.setContentWithKoin
import com.connectapp.core.uitest.textField
import com.connectapp.domain.professionals.ProfessionalRepository
import com.connectapp.domain.professionals.di.professionalsDomainModule
import com.connectapp.domain.shared.professional.ProfessionalCollege
import com.connectapp.presentation.professionals.detail.ProfessionalDetailScreen
import com.connectapp.presentation.professionals.di.professionalsModule
import com.connectapp.presentation.professionals.form.ProfessionalFormScreen
import org.koin.dsl.module
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Professional registration with NIF/NIE and college (D9), and archiving
 * only after confirmation (D8). Real Koin wiring; only the repository is a fake.
 */
@OptIn(ExperimentalTestApi::class)
class ProfessionalScreensUiTest : UiTest() {

    private val repository = FakeProfessionalRepository()
    private val text = Texts()
    private val modules = arrayOf(
        module { single<ProfessionalRepository> { repository } },
        professionalsDomainModule,
        professionalsModule,
    )

    @Test
    fun `a professional with an invalid NIF is rejected and a valid one is saved`() = runComposeUiTest {
        var savedId: String? = null
        setContentWithKoin(*modules) {
            text.resolve(
                label_nif, label_college, label_license_number, label_first_name, label_last_name,
                button_save, error_invalid_nif,
            )
            ProfessionalFormScreen(professionalId = null, onSaved = { savedId = it })
        }
        val save = clickable(text[button_save])

        textField(text[label_nif]).performTextInput("12345678A")
        textField(text[label_license_number]).performTextInput("28/1234")
        onNodeWithText(text[error_invalid_nif]).assertExists()

        textField(text[label_nif]).performTextReplacement("12345678Z")
        onNode(hasClickAction() and hasText(text[label_college])).performClick()
        // The option lives in a popup: invoke its click action, as a screen reader would.
        onNodeWithText(ProfessionalCollege.MADRID.displayName).performSemanticsAction(SemanticsActions.OnClick)
        onNode(hasText(text[label_college]) and hasText(ProfessionalCollege.MADRID.displayName)).assertExists()
        textField(text[label_first_name]).performTextInput("Ana")
        save.performScrollTo().assertIsNotEnabled()
        textField(text[label_last_name]).performTextInput("Gil")
        save.performScrollTo().assertIsEnabled()
        save.performClick()
        waitForIdle()

        assertEquals("pro-1", savedId)
        assertEquals(ProfessionalCollege.MADRID, repository.records.value.getValue("pro-1").college)
    }

    @Test
    fun `archiving a professional asks for confirmation first`() = runComposeUiTest {
        repository.add("pro-1")
        setContentWithKoin(*modules) {
            text.resolve(button_archive)
            ProfessionalDetailScreen(professionalId = "pro-1", onEdit = {}, onDeleted = {})
        }
        waitForIdle()

        clickable(text[button_archive]).performScrollTo().performClick()
        waitForIdle()
        assertTrue(!repository.records.value.getValue("pro-1").isArchived, "Nothing changes before confirming")

        onNode(hasClickAction() and hasText(text[button_archive]) and hasAnyAncestor(isDialog())).performClick()
        waitForIdle()

        assertTrue(repository.records.value.getValue("pro-1").isArchived)
    }
}
