package com.connectapp.presentation.forgot_password

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import com.connectapp.commonresources.button_back_to_login
import com.connectapp.commonresources.button_reset_password
import com.connectapp.commonresources.forgot_password_error_invalid_data
import com.connectapp.commonresources.label_confirm_new_password
import com.connectapp.commonresources.label_email
import com.connectapp.commonresources.label_new_password
import com.connectapp.commonresources.label_recovery_code
import com.connectapp.commonresources.recovery_code_confirm
import com.connectapp.core.uitest.Texts
import com.connectapp.core.uitest.UiTest
import com.connectapp.core.uitest.clickable
import com.connectapp.core.uitest.setContentWithKoin
import com.connectapp.core.uitest.textField
import com.connectapp.domain.model.AuthError
import com.connectapp.domain.usecase.ResetPasswordUseCase
import com.connectapp.presentation.forgot_password.fake.FakeAuthRepository
import com.connectapp.presentation.forgot_password.fake.RECOVERY_CODE
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import kotlin.test.Test
import kotlin.test.assertTrue

/** Password recovery without revealing whether the email exists (D4). */
@OptIn(ExperimentalTestApi::class)
class ForgotPasswordScreenUiTest : UiTest() {

    private val repository = FakeAuthRepository()
    private val text = Texts()
    private var wentToLogin = false

    private fun ComposeUiTest.showAndSubmit() {
        setContentWithKoin(module { viewModel { ForgotPasswordViewModel(ResetPasswordUseCase(repository)) } }) {
            text.resolve(
                label_email, label_recovery_code, label_new_password, label_confirm_new_password,
                button_reset_password, forgot_password_error_invalid_data, recovery_code_confirm,
                button_back_to_login,
            )
            ForgotPasswordScreen(onNavigateBack = { wentToLogin = true })
        }
        textField(text[label_email]).performTextInput("aina@clinica.es")
        textField(text[label_recovery_code]).performTextInput("23456-789ab-cdefg-hjklm")
        textField(text[label_new_password]).performTextInput("Nueva-Clave-2026")
        textField(text[label_confirm_new_password]).performTextInput("Nueva-Clave-2026")
        clickable(text[button_reset_password]).performClick()
        waitForIdle()
    }

    @Test
    fun `a wrong code or unknown email shows the same neutral message`() = runComposeUiTest {
        repository.resetResult = Result.failure(AuthError.InvalidRecoveryData)

        showAndSubmit()

        onNodeWithText(text[forgot_password_error_invalid_data]).assertExists()
    }

    @Test
    fun `a valid reset shows the new code and returns to login once confirmed`() = runComposeUiTest {
        showAndSubmit()

        onNodeWithText(RECOVERY_CODE).assertExists()
        // Two "back to login" buttons never coexist: the form one is gone with the form.
        clickable(text[button_back_to_login]).assertIsNotEnabled()
        onNode(isToggleable() and hasText(text[recovery_code_confirm])).performClick()
        clickable(text[button_back_to_login]).performClick()
        waitForIdle()

        assertTrue(wentToLogin)
    }
}
