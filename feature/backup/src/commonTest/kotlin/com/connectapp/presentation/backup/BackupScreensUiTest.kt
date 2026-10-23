package com.connectapp.presentation.backup

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.runComposeUiTest
import com.connectapp.commonresources.backup_file_selected
import com.connectapp.commonresources.backup_restore_confirm_title
import com.connectapp.commonresources.button_create_backup
import com.connectapp.commonresources.button_replace_data
import com.connectapp.commonresources.button_restore_backup
import com.connectapp.commonresources.label_backup_password
import com.connectapp.commonresources.label_confirm_backup_password
import com.connectapp.commonresources.label_your_password
import com.connectapp.commonresources.settings_backup_never
import com.connectapp.core.uitest.Texts
import com.connectapp.core.uitest.UiTest
import com.connectapp.core.uitest.clickable
import com.connectapp.core.uitest.setContentWithKoin
import com.connectapp.core.uitest.textField
import com.connectapp.domain.backup.CreateBackupUseCase
import com.connectapp.domain.backup.MarkBackupSavedUseCase
import com.connectapp.domain.backup.ObserveLastBackupUseCase
import com.connectapp.domain.backup.RestoreBackupUseCase
import com.connectapp.presentation.backup.create.CreateBackupScreen
import com.connectapp.presentation.backup.create.CreateBackupViewModel
import com.connectapp.presentation.backup.fake.FakeAuthRepository
import com.connectapp.presentation.backup.fake.FakeBackupRepository
import com.connectapp.presentation.backup.restore.RestoreBackupIntent
import com.connectapp.presentation.backup.restore.RestoreBackupScreen
import com.connectapp.presentation.backup.restore.RestoreBackupViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Plan §21.7: the backup screens rendered for real. The system file pickers are not exercised. */
@OptIn(ExperimentalTestApi::class)
class BackupScreensUiTest : UiTest() {

    private val backups = FakeBackupRepository()
    private val auth = FakeAuthRepository()
    private val text = Texts()
    private val strong = "Copia-Clinica-2026!"
    private var fileLabel = ""

    private fun ComposeUiTest.showCreate() = setContentWithKoin(
        module {
            viewModel {
                CreateBackupViewModel(
                    CreateBackupUseCase(backups),
                    MarkBackupSavedUseCase(backups),
                    ObserveLastBackupUseCase(backups),
                )
            }
        },
    ) {
        text.resolve(label_backup_password, label_confirm_backup_password, button_create_backup, settings_backup_never)
        CreateBackupScreen()
    }

    /** The file is fed straight to the ViewModel, as the system picker would after a choice. */
    private fun ComposeUiTest.showRestore(fromSignIn: Boolean, onRestored: () -> Unit = {}) {
        val viewModel = RestoreBackupViewModel(fromSignIn, RestoreBackupUseCase(backups, auth))
        viewModel.onIntent(RestoreBackupIntent.FilePicked("copia.physiobackup", byteArrayOf(1)))
        setContentWithKoin(module { viewModel { viewModel } }) {
            text.resolve(
                label_backup_password, label_your_password, button_restore_backup,
                backup_restore_confirm_title, button_replace_data,
            )
            fileLabel = stringResource(backup_file_selected, "copia.physiobackup")
            RestoreBackupScreen(fromSignIn = fromSignIn, onRestored = onRestored, onNavigateToLogin = {})
        }
    }

    @Test
    fun `creating a backup needs a strong password typed twice`() = runComposeUiTest {
        showCreate()
        val create = clickable(text[button_create_backup])

        onNodeWithText(text[settings_backup_never]).assertExists()
        create.assertIsNotEnabled()
        textField(text[label_backup_password]).performTextInput(strong)
        textField(text[label_confirm_backup_password]).performTextInput("$strong.")
        create.assertIsNotEnabled()
        textField(text[label_confirm_backup_password]).performTextReplacement(strong)
        create.assertIsEnabled()
    }

    @Test
    fun `the sign-in screen restores after confirming without a session password`() = runComposeUiTest {
        var restored = false
        showRestore(fromSignIn = true, onRestored = { restored = true })

        onNodeWithText(fileLabel).assertExists()
        onNodeWithText(text[label_your_password]).assertDoesNotExist()
        textField(text[label_backup_password]).performTextInput(strong)
        clickable(text[button_restore_backup]).performClick()

        onNodeWithText(text[backup_restore_confirm_title]).assertExists()
        // Robolectric ignores simulated touches on dialogs (popups): use the click action.
        onNodeWithText(text[button_replace_data]).performSemanticsAction(SemanticsActions.OnClick)
        waitForIdle()

        assertTrue(restored)
        assertEquals(true, backups.lastRestore?.third)
    }

    @Test
    fun `settings also asks for the session password`() = runComposeUiTest {
        showRestore(fromSignIn = false)
        val restore = clickable(text[button_restore_backup])

        textField(text[label_backup_password]).performTextInput(strong)
        restore.assertIsNotEnabled()
        textField(text[label_your_password]).performTextInput("Mi-Clave-Segura-1")
        restore.assertIsEnabled()
    }
}
