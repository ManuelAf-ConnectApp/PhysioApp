package com.connectapp.presentation.settings

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.runComposeUiTest
import com.connectapp.commonresources.button_back_to_settings
import com.connectapp.commonresources.button_generate_code
import com.connectapp.commonresources.change_password_success
import com.connectapp.commonresources.error_wrong_password
import com.connectapp.commonresources.label_confirm_new_password
import com.connectapp.commonresources.label_current_password
import com.connectapp.commonresources.label_new_password
import com.connectapp.commonresources.label_password
import com.connectapp.commonresources.recovery_code_confirm
import com.connectapp.commonresources.settings_backup_never
import com.connectapp.commonresources.settings_item_backup_create
import com.connectapp.commonresources.settings_item_backup_restore
import com.connectapp.commonresources.settings_item_change_password
import com.connectapp.commonresources.settings_item_licenses
import com.connectapp.commonresources.settings_item_recovery_code
import com.connectapp.commonresources.settings_section_backup
import com.connectapp.commonresources.settings_section_security
import com.connectapp.core.uitest.Texts
import com.connectapp.core.uitest.UiTest
import com.connectapp.core.uitest.clickable
import com.connectapp.core.uitest.setContentWithKoin
import com.connectapp.core.uitest.textField
import com.connectapp.domain.backup.ObserveLastBackupUseCase
import com.connectapp.domain.model.AuthError
import com.connectapp.domain.provider.AppVersionProvider
import com.connectapp.domain.usecase.ChangePasswordUseCase
import com.connectapp.domain.usecase.LogoutUseCase
import com.connectapp.domain.usecase.ObserveCurrentAccountUseCase
import com.connectapp.domain.usecase.RegenerateRecoveryCodeUseCase
import com.connectapp.presentation.settings.fake.FakeAuthRepository
import com.connectapp.presentation.settings.fake.FakeBackupRepository
import com.connectapp.presentation.settings.fake.RECOVERY_CODE
import com.connectapp.presentation.settings.fake.accountWithProfile
import com.connectapp.presentation.settings.password.ChangePasswordScreen
import com.connectapp.presentation.settings.password.ChangePasswordViewModel
import com.connectapp.presentation.settings.recoverycode.RegenerateRecoveryCodeScreen
import com.connectapp.presentation.settings.recoverycode.RegenerateRecoveryCodeViewModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/** Settings screens (D15) rendered for real: semantics, enabled states and the flows. */
@OptIn(ExperimentalTestApi::class)
class SettingsScreensUiTest : UiTest() {

    private val repository = FakeAuthRepository().apply { account.value = accountWithProfile }

    private val text = Texts()

    private fun ComposeUiTest.showChangePassword(onDone: () -> Unit = {}, onLogin: () -> Unit = {}) =
        setContentWithKoin(module { viewModel { ChangePasswordViewModel(ChangePasswordUseCase(repository)) } }) {
            text.resolve(
                label_current_password, label_new_password, label_confirm_new_password,
                settings_item_change_password, change_password_success, button_back_to_settings,
                error_wrong_password,
            )
            ChangePasswordScreen(onDone = onDone, onNavigateToLogin = onLogin)
        }

    private fun ComposeUiTest.showRegenerateCode(onDone: () -> Unit = {}) = setContentWithKoin(
        module { viewModel { RegenerateRecoveryCodeViewModel(RegenerateRecoveryCodeUseCase(repository)) } },
    ) {
        text.resolve(label_password, button_generate_code, recovery_code_confirm, button_back_to_settings)
        RegenerateRecoveryCodeScreen(onDone = onDone, onNavigateToLogin = {})
    }

    private fun ComposeUiTest.fillChangePassword(current: String, confirmation: String = NEW) {
        textField(text[label_current_password]).performTextInput(current)
        textField(text[label_new_password]).performTextInput(NEW)
        textField(text[label_confirm_new_password]).performTextInput(confirmation)
    }

    @Test
    fun `settings lists the security actions under a heading and opens them`() = runComposeUiTest {
        var opened: SettingsDestination? = null
        setContentWithKoin(
            module {
                viewModel {
                    SettingsViewModel(
                        LogoutUseCase(repository),
                        ObserveCurrentAccountUseCase(repository),
                        object : AppVersionProvider { override val versionName = "1.0-test" },
                        ObserveLastBackupUseCase(FakeBackupRepository()),
                    )
                }
            },
        ) {
            text.resolve(
                settings_section_security, settings_item_change_password, settings_item_recovery_code,
                settings_item_licenses, settings_section_backup, settings_item_backup_create,
                settings_item_backup_restore, settings_backup_never,
            )
            SettingsScreen(onLogout = {}, onNavigate = { opened = it })
        }

        onNode(isHeading() and hasText(text[settings_section_security])).assertExists()
        clickable(text[settings_item_change_password]).performClick()
        assertEquals(SettingsDestination.CHANGE_PASSWORD, opened)
        clickable(text[settings_item_recovery_code]).performClick()
        assertEquals(SettingsDestination.RECOVERY_CODE, opened)
        onNode(isHeading() and hasText(text[settings_section_backup])).assertExists()
        onNodeWithText(text[settings_backup_never]).assertExists()
        clickable(text[settings_item_backup_create]).performScrollTo().performClick()
        assertEquals(SettingsDestination.CREATE_BACKUP, opened)
        clickable(text[settings_item_backup_restore]).performScrollTo().performClick()
        assertEquals(SettingsDestination.RESTORE_BACKUP, opened)
        clickable(text[settings_item_licenses]).performScrollTo().performClick()
        assertEquals(SettingsDestination.LICENSES, opened)
    }

    @Test
    fun `change password is enabled only with a valid form and then confirms`() = runComposeUiTest {
        var done = false
        showChangePassword(onDone = { done = true })
        val submit = clickable(text[settings_item_change_password])

        submit.assertIsNotEnabled()
        fillChangePassword(CURRENT, confirmation = "Otra-Clave-2026")
        submit.assertIsNotEnabled()
        textField(text[label_confirm_new_password]).performTextReplacement(NEW)
        submit.assertIsEnabled()
        submit.performClick()
        waitForIdle()

        assertEquals(listOf(CURRENT, NEW), repository.lastChangePasswordArgs)
        onNodeWithText(text[change_password_success]).assertExists()
        clickable(text[button_back_to_settings]).performClick()
        assertTrue(done)
    }

    @Test
    fun `a wrong current password shows the error and keeps the form`() = runComposeUiTest {
        repository.changePasswordResult = Result.failure(AuthError.InvalidCredentials)
        showChangePassword()

        fillChangePassword(CURRENT)
        clickable(text[settings_item_change_password]).performClick()
        waitForIdle()

        onNodeWithText(text[error_wrong_password]).assertExists()
        textField(text[label_current_password]).assertExists()
    }

    @Test
    fun `the new recovery code can only be left after confirming it is stored`() = runComposeUiTest {
        var done = false
        showRegenerateCode(onDone = { done = true })
        val generate = clickable(text[button_generate_code])

        generate.assertIsNotEnabled()
        textField(text[label_password]).performTextInput(CURRENT)
        generate.performClick()
        waitForIdle()

        onNodeWithText(RECOVERY_CODE).assertExists()
        val back = clickable(text[button_back_to_settings])
        back.assertIsNotEnabled()
        val confirm = onNode(isToggleable() and hasText(text[recovery_code_confirm]))
        confirm.assertIsOff()
        confirm.performClick()
        confirm.assertIsOn()
        back.assertIsEnabled()
        back.performClick()
        waitForIdle()

        assertTrue(done)
        assertNull(repository.lastChangePasswordArgs)
    }

    private companion object {
        const val CURRENT = "Actual-Clave-2026"
        const val NEW = "Nueva-Clave-2026"
    }
}
