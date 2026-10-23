package com.connectapp.presentation

import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import androidx.navigation3.runtime.NavEntry
import com.connectapp.commonresources.back
import com.connectapp.commonresources.button_discard
import com.connectapp.commonresources.button_keep_editing
import com.connectapp.commonresources.discard_changes_title
import com.connectapp.commonresources.home_patient_register_title
import com.connectapp.commonresources.nav_settings
import com.connectapp.commonresources.navigation.GuardUnsavedChanges
import com.connectapp.core.uitest.Texts
import com.connectapp.core.uitest.UiTest
import com.connectapp.core.uitest.clickable
import com.connectapp.core.uitest.setContentWithKoin
import com.connectapp.domain.policy.EpochClock
import com.connectapp.domain.usecase.ExpireIdleSessionUseCase
import com.connectapp.domain.usecase.RecordUserActivityUseCase
import com.connectapp.presentation.fake.FakeAuthRepository
import com.connectapp.presentation.fake.accountWithProfile
import com.connectapp.presentation.navigation.NavigationRoute
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The shell rendered for real: title heading, back arrow and bottom bar, and the confirmation
 * before leaving a screen with unsaved changes. The feature screens are
 * replaced by plain placeholders; one of them reports unsaved changes like a half-filled form.
 */
@OptIn(ExperimentalTestApi::class)
class MainScreenUiTest : UiTest() {

    private val now = 1_000_000L
    private val repository = FakeAuthRepository().apply {
        account.value = accountWithProfile
        lastActivity = now
    }
    private val text = Texts()
    private lateinit var shell: MainViewModel

    private fun ComposeUiTest.showShellOnADirtyForm() =
        setContentWithKoin(
            module {
                viewModel {
                    MainViewModel(RecordUserActivityUseCase(repository), ExpireIdleSessionUseCase(repository, EpochClock { now }))
                }
            },
        ) {
            text.resolve(back, discard_changes_title, button_discard, button_keep_editing, nav_settings, home_patient_register_title)
            shell = koinViewModel()
            MainScreen(entryProvider = { key ->
                NavEntry(key) {
                    if (key == NavigationRoute.PatientRegisterRoute) {
                        GuardUnsavedChanges(dirty = true)
                        Text(FORM)
                    } else {
                        Text(key.toString())
                    }
                }
            })
            LaunchedEffect(Unit) {
                shell.updateNavigationState(NavigationRoute.HomeRoute)
                shell.updateNavigationState(NavigationRoute.PatientRegisterRoute)
            }
        }

    @Test
    fun `the screen title is announced as a heading`() = runComposeUiTest {
        showShellOnADirtyForm()
        waitForIdle()

        onNode(isHeading() and hasText(text[home_patient_register_title])).assertExists()
    }

    @Test
    fun `leaving a form with unsaved changes asks and keep editing stays`() = runComposeUiTest {
        showShellOnADirtyForm()
        waitForIdle()

        onNodeWithContentDescription(text[back]).performClick()
        waitForIdle()
        onNodeWithText(text[discard_changes_title]).assertExists()
        clickable(text[button_keep_editing]).performClick()
        waitForIdle()

        onNodeWithText(FORM).assertExists()
        assertEquals(NavigationRoute.PatientRegisterRoute, shell.navState.value.route)
    }

    @Test
    fun `discarding the changes leaves through the bottom bar`() = runComposeUiTest {
        showShellOnADirtyForm()
        waitForIdle()

        clickable(text[nav_settings]).performClick()
        waitForIdle()
        clickable(text[button_discard]).performClick()
        waitForIdle()

        assertEquals(NavigationRoute.SettingsRoute, shell.navState.value.route)
        onNodeWithText(FORM).assertDoesNotExist()
    }

    private companion object {
        const val FORM = "half-filled form"
    }
}
