package com.connectapp.presentation.register

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import com.connectapp.commonresources.button_go_to_login
import com.connectapp.commonresources.button_register
import com.connectapp.commonresources.error_invalid_email
import com.connectapp.commonresources.error_passwords_not_match
import com.connectapp.commonresources.label_confirm_password
import com.connectapp.commonresources.label_email
import com.connectapp.commonresources.label_first_name
import com.connectapp.commonresources.label_last_name
import com.connectapp.commonresources.label_password
import com.connectapp.commonresources.recovery_code_confirm
import com.connectapp.commonresources.register_error_email_already_registered
import com.connectapp.core.uitest.Texts
import com.connectapp.core.uitest.UiTest
import com.connectapp.core.uitest.clickable
import com.connectapp.core.uitest.setContentWithKoin
import com.connectapp.core.uitest.textField
import com.connectapp.domain.model.AuthError
import com.connectapp.domain.usecase.RegisterUseCase
import com.connectapp.presentation.register.fake.FakeAuthRepository
import com.connectapp.presentation.register.fake.RECOVERY_CODE
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Registration validation, duplicate email and the one-time code (D4). */
@OptIn(ExperimentalTestApi::class)
class RegisterScreenUiTest : UiTest() {

    private val repository = FakeAuthRepository()
    private val text = Texts()
    private var wentToLogin = false

    private fun ComposeUiTest.showRegister() =
        setContentWithKoin(module { viewModel { RegisterViewModel(RegisterUseCase(repository)) } }) {
            text.resolve(
                label_first_name, label_last_name, label_email, label_password, label_confirm_password,
                button_register, error_invalid_email, error_passwords_not_match,
                register_error_email_already_registered, recovery_code_confirm, button_go_to_login,
            )
            RegisterScreen(onNavigateToLogin = { wentToLogin = true })
        }

    private fun ComposeUiTest.fillForm(email: String = "aina@clinica.es", confirmation: String = PASSWORD) {
        textField(text[label_first_name]).performTextInput("Aina")
        textField(text[label_last_name]).performTextInput("García")
        textField(text[label_email]).performTextInput(email)
        textField(text[label_password]).performTextInput(PASSWORD)
        textField(text[label_confirm_password]).performTextInput(confirmation)
    }

    // "cuando salgo del campo, veo un error junto al campo".
    @Test
    fun `an invalid email shows its error once the field is left and blocks the button`() = runComposeUiTest {
        showRegister()

        fillForm(email = "aina-clinica.es")

        onNodeWithText(text[error_invalid_email]).assertExists()
        clickable(text[button_register]).assertIsNotEnabled()
    }

    @Test
    fun `a confirmation that does not match is reported`() = runComposeUiTest {
        showRegister()

        fillForm(confirmation = "Otra-Clave-2026")
        textField(text[label_first_name]).performClick() // leave the confirmation field

        onNodeWithText(text[error_passwords_not_match]).assertExists()
        clickable(text[button_register]).assertIsNotEnabled()
    }

    @Test
    fun `an email already registered is rejected`() = runComposeUiTest {
        repository.registerResult = Result.failure(AuthError.EmailAlreadyRegistered)
        showRegister()

        fillForm()
        clickable(text[button_register]).performClick()
        waitForIdle()

        onNodeWithText(text[register_error_email_already_registered]).assertExists()
    }

    @Test
    fun `a valid registration shows the recovery code until it is confirmed`() = runComposeUiTest {
        showRegister()

        fillForm()
        clickable(text[button_register]).assertIsEnabled()
        clickable(text[button_register]).performClick()
        waitForIdle()

        assertEquals(listOf("Aina", "García", "aina@clinica.es", PASSWORD), repository.lastRegisterArgs)
        onNodeWithText(RECOVERY_CODE).assertExists()
        clickable(text[button_go_to_login]).assertIsNotEnabled()
        onNode(isToggleable() and hasText(text[recovery_code_confirm])).performClick()
        clickable(text[button_go_to_login]).performClick()
        waitForIdle()
        assertTrue(wentToLogin)
    }

    private companion object {
        const val PASSWORD = "Fisio-Clinica-2026"
    }
}
