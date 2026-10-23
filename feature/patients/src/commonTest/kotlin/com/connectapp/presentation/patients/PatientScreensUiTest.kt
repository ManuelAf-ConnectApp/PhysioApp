package com.connectapp.presentation.patients

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.test.swipe
import com.connectapp.commonresources.adaptive.LocalWindowWidthClass
import com.connectapp.commonresources.adaptive.WindowWidthClass
import com.connectapp.commonresources.badge_archived
import com.connectapp.commonresources.button_delete_permanently
import com.connectapp.commonresources.button_edit
import com.connectapp.commonresources.button_save
import com.connectapp.commonresources.delete_patient_title
import com.connectapp.commonresources.error_signature_required
import com.connectapp.commonresources.label_document_number
import com.connectapp.commonresources.label_first_name
import com.connectapp.commonresources.label_last_name
import com.connectapp.commonresources.search_show_archived
import com.connectapp.commonresources.signature_image_description
import com.connectapp.commonresources.signature.renderSignaturePng
import com.connectapp.commonresources.signature_pad_description
import com.connectapp.core.uitest.Texts
import com.connectapp.core.uitest.UiTest
import com.connectapp.core.uitest.clickable
import com.connectapp.core.uitest.setContentWithKoin
import com.connectapp.core.uitest.textField
import com.connectapp.domain.patients.ConsentSignature
import com.connectapp.domain.patients.PatientRepository
import com.connectapp.domain.patients.SignatureSigner
import com.connectapp.domain.patients.di.patientsDomainModule
import com.connectapp.presentation.patients.detail.PatientDetailScreen
import com.connectapp.presentation.patients.di.patientsModule
import com.connectapp.presentation.patients.form.PatientFormScreen
import com.connectapp.presentation.patients.search.PatientSearchScreen
import org.koin.dsl.module
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Patient registration with the consent signature (D13), excluding
 * search with archived records hidden by default (D8) and confirmed deletion. Real Koin wiring
 * of the feature and domain modules; only the repository is a fake.
 */
@OptIn(ExperimentalTestApi::class)
class PatientScreensUiTest : UiTest() {

    private val repository = FakePatientRepository()
    private val text = Texts()
    private val modules = arrayOf(
        module { single<PatientRepository> { repository } },
        patientsDomainModule,
        patientsModule,
    )

    private fun ComposeUiTest.signOnThePad() {
        onNodeWithContentDescription(text[signature_pad_description]).performScrollTo().performTouchInput {
            swipe(start = centerLeft, end = centerRight, durationMillis = 200)
        }
    }

    @Test
    fun `a new patient needs the consent signature and is saved with its image`() = runComposeUiTest {
        var savedId: String? = null
        setContentWithKoin(*modules) {
            text.resolve(
                label_document_number, label_first_name, label_last_name, button_save,
                error_signature_required, signature_pad_description,
            )
            PatientFormScreen(patientId = null, onSaved = { savedId = it })
        }
        val save = clickable(text[button_save])

        textField(text[label_document_number]).performTextInput("X1234567L")
        textField(text[label_first_name]).performTextInput("Pepe")
        textField(text[label_last_name]).performTextInput("Ruiz")
        onNodeWithText(text[error_signature_required]).assertExists()
        save.assertIsNotEnabled()

        signOnThePad()
        onNodeWithText(text[error_signature_required]).assertDoesNotExist()
        save.performScrollTo().assertIsEnabled()
        save.performClick()
        waitForIdle()

        assertEquals("pat-1", savedId)
        val signature = repository.signatures.getValue("pat-1").single()
        assertEquals(SignatureSigner.PATIENT, signature.signer)
        // A real PNG rendered from the strokes: it starts with the PNG signature bytes.
        assertTrue(signature.pngBytes.size > PNG_MAGIC.size)
        assertEquals(PNG_MAGIC.toList(), signature.pngBytes.take(PNG_MAGIC.size))
    }

    @Test
    fun `archived patients are hidden until shown and selecting one opens it beside the list`() = runComposeUiTest {
        repository.add("pat-1", pepeDraft)
        repository.add("pat-2", pepeDraft.copy(documentNumber = "Y7654321G", firstName = "Pepa"), archived = true)
        setContentWithKoin(*modules) {
            text.resolve(search_show_archived, badge_archived, button_edit)
            CompositionLocalProvider(LocalWindowWidthClass provides WindowWidthClass.EXPANDED) {
                PatientSearchScreen(onOpenDetail = {}, onEdit = {}, onSignAgain = {})
            }
        }
        waitForIdle()

        onNodeWithText("Pepe Ruiz").assertExists()
        onNodeWithText("Pepa Ruiz").assertDoesNotExist()
        onNode(hasText(text[search_show_archived]) and hasClickAction()).performClick()
        waitForIdle()
        onNodeWithText("Pepa Ruiz").assertExists()
        onNodeWithText(text[badge_archived]).assertExists()

        val row = onNode(isSelectable() and hasText("Pepe Ruiz"))
        row.performClick()
        waitForIdle()
        row.assertIsSelected()
        // The detail pane opens next to the list, on the same screen.
        clickable(text[button_edit]).assertExists()
    }

    @Test
    fun `on a narrow window a result opens the detail screen instead`() = runComposeUiTest {
        repository.add("pat-1", pepeDraft)
        var opened: String? = null
        setContentWithKoin(*modules) {
            CompositionLocalProvider(LocalWindowWidthClass provides WindowWidthClass.COMPACT) {
                PatientSearchScreen(onOpenDetail = { opened = it }, onEdit = {}, onSignAgain = {})
            }
        }
        waitForIdle()

        onNodeWithText("Pepe Ruiz").performClick()

        assertEquals("pat-1", opened)
    }

    @Test
    fun `deleting a patient asks for confirmation first`() = runComposeUiTest {
        repository.add("pat-1", pepeDraft)
        var deleted = false
        setContentWithKoin(*modules) {
            text.resolve(button_delete_permanently, delete_patient_title)
            PatientDetailScreen(patientId = "pat-1", onEdit = {}, onSignAgain = {}, onDeleted = { deleted = true })
        }
        waitForIdle()

        clickable(text[button_delete_permanently]).performScrollTo().performClick()
        waitForIdle()
        onNode(isDialog()).assertExists()
        onNodeWithText(text[delete_patient_title]).assertExists()
        assertTrue(repository.records.value.containsKey("pat-1"), "Nothing is deleted before confirming")

        onNode(hasClickAction() and hasText(text[button_delete_permanently]) and hasAnyAncestor(isDialog())).performClick()
        waitForIdle()

        assertNull(repository.records.value["pat-1"])
        assertTrue(deleted)
    }

    // The detail shows the stored signature image with a description for screen readers.
    @Test
    fun `the detail shows the signature image`() = runComposeUiTest {
        repository.add("pat-1", pepeDraft)
        repository.signatures["pat-1"] = mutableListOf(signatureWithRealPng())
        repository.add("pat-1", pepeDraft) // refresh the record so it carries the signature info
        setContentWithKoin(*modules) {
            text.resolve(signature_image_description)
            PatientDetailScreen(patientId = "pat-1", onEdit = {}, onSignAgain = {}, onDeleted = {})
        }
        waitForIdle()

        onNode(hasContentDescription(text[signature_image_description])).assertExists()
    }

    private fun signatureWithRealPng() = ConsentSignature(
        pngBytes = renderSignaturePng(listOf(listOf(Offset(0.1f, 0.5f), Offset(0.9f, 0.5f)))),
        signer = SignatureSigner.PATIENT,
    )

    private companion object {
        val PNG_MAGIC = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)
    }
}
