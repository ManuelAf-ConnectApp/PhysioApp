package com.connectapp.presentation.login

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import com.connectapp.commonresources.button_login
import com.connectapp.commonresources.label_email
import com.connectapp.commonresources.label_password
import com.connectapp.commonresources.login_error_invalid_credentials
import com.connectapp.commonresources.login_remember_session
import com.connectapp.commonresources.login_restore_backup
import com.connectapp.core.uitest.Texts
import com.connectapp.core.uitest.UiTest
import com.connectapp.core.uitest.clickable
import com.connectapp.core.uitest.setContentWithKoin
import com.connectapp.core.uitest.textField
import com.connectapp.domain.model.AuthError
import com.connectapp.domain.policy.EpochClock
import com.connectapp.domain.usecase.HasAccountsUseCase
import com.connectapp.domain.usecase.LoginUseCase
import com.connectapp.presentation.login.fake.FakeAuthRepository
import com.connectapp.presentation.login.fake.accountWithoutProfile
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Valid and invalid login, rendered for real. */
@OptIn(ExperimentalTestApi::class)
class LoginScreenUiTest : UiTest() {

    private val repository = FakeAuthRepository()
    private val text = Texts()
    private var destination = ""

    private fun ComposeUiTest.showLogin() =
        setContentWithKoin(module { viewModel { LoginViewModel(LoginUseCase(repository), EpochClock { 0L }, HasAccountsUseCase(repository)) } }) {
            text.resolve(
                label_email, label_password, button_login, login_error_invalid_credentials, login_remember_session,
                login_restore_backup,
            )
            LoginScreen(
                onNavigateToRegister = { destination = "register" },
                onNavigateToForgotPassword = { destination = "forgot" },
                onNavigateToHome = { destination = "home" },
                onNavigateToOnboarding = { destination = "onboarding" },
                onNavigateToRestoreBackup = { destination = "restore" },
            )
        }

    private fun ComposeUiTest.signIn(password: String = "Fisio-Clinica-2026") {
        textField(text[label_email]).performTextInput("aina@clinica.es")
        textField(text[label_password]).performTextInput(password)
        clickable(text[button_login]).performClick()
        waitForIdle()
    }

    @Test
    fun `the login button waits for email and password`() = runComposeUiTest {
        showLogin()
        val login = clickable(text[button_login])

        login.assertIsNotEnabled()
        textField(text[label_email]).performTextInput("aina@clinica.es")
        login.assertIsNotEnabled()
        textField(text[label_password]).performTextInput("x")
        login.assertIsEnabled()
    }

    @Test
    fun `valid credentials of a complete account open home`() = runComposeUiTest {
        showLogin()

        signIn()

        assertEquals("home", destination)
        assertEquals(false, repository.lastRememberSession)
    }

    @Test
    fun `a first login without professional profile opens the onboarding`() = runComposeUiTest {
        repository.loginResult = Result.success(accountWithoutProfile)
        showLogin()

        signIn()

        assertEquals("onboarding", destination)
    }

    @Test
    fun `wrong credentials show the neutral error and stay on login`() = runComposeUiTest {
        repository.loginResult = Result.failure(AuthError.InvalidCredentials)
        showLogin()

        signIn(password = "Mala-Clave-2026")

        onNodeWithText(text[login_error_invalid_credentials]).assertExists()
        assertEquals("", destination)
    }

    // D14: "remember session" is off by default and the whole row is one checkbox.
    @Test
    fun `remember session is off by default and is sent when ticked`() = runComposeUiTest {
        showLogin()
        val remember = onNode(isToggleable() and hasText(text[login_remember_session]))

        remember.assertIsOff()
        remember.performClick()
        remember.assertIsOn()
        signIn()

        assertTrue(repository.lastRememberSession == true)
    }

    @Test
    fun `a tablet without accounts offers restoring a backup`() = runComposeUiTest {
        repository.hasAccountsResult = false
        showLogin()
        waitForIdle()

        clickable(text[login_restore_backup]).performClick()

        assertEquals("restore", destination)
    }

    @Test
    fun `a tablet with accounts hides the restore option`() = runComposeUiTest {
        showLogin()
        waitForIdle()

        onNodeWithText(text[login_restore_backup]).assertDoesNotExist()
    }
}
