package com.connectapp.presentation.home

import com.connectapp.commonresources.adaptive.WindowWidthClass
import com.connectapp.domain.backup.BackupReminder
import com.connectapp.domain.backup.ObserveBackupReminderUseCase
import com.connectapp.domain.usecase.ObserveCurrentAccountUseCase
import com.connectapp.presentation.home.fake.FakeAuthRepository
import com.connectapp.presentation.home.fake.FakeBackupRepository
import com.connectapp.presentation.home.fake.accountWithProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val repository = FakeAuthRepository()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private val backups = FakeBackupRepository()
    private var now = 0L

    private fun viewModel() = HomeViewModel(
        ObserveCurrentAccountUseCase(repository),
        ObserveBackupReminderUseCase(backups) { now },
    )

    // "ocho tarjetas de acceso", in this order.
    @Test
    fun `home offers the eight sections in pairs per entity`() {
        assertEquals(
            listOf(
                HomeDestination.PATIENT_REGISTER, HomeDestination.PATIENT_SEARCH,
                HomeDestination.PROFESSIONAL_REGISTER, HomeDestination.PROFESSIONAL_SEARCH,
                HomeDestination.REPORT_CREATE, HomeDestination.REPORT_SEARCH,
                HomeDestination.INVOICE_CREATE, HomeDestination.INVOICE_SEARCH,
            ),
            HomeState().destinations,
        )
    }

    @Test
    fun `clicking a card navigates to its section`() = runTest {
        val viewModel = viewModel()
        val effects = mutableListOf<HomeEffect>()
        val job = launch { viewModel.effect.toList(effects) }

        viewModel.onIntent(HomeIntent.DestinationClicked(HomeDestination.INVOICE_SEARCH))
        advanceUntilIdle()

        assertEquals(listOf<HomeEffect>(HomeEffect.NavigateTo(HomeDestination.INVOICE_SEARCH)), effects)
        job.cancel()
    }

    @Test
    fun `the greeting uses the first name of the signed-in account`() = runTest {
        val viewModel = viewModel()
        advanceUntilIdle()
        assertNull(viewModel.state.value.firstName)

        repository.account.value = accountWithProfile
        advanceUntilIdle()

        assertEquals(accountWithProfile.firstName, viewModel.state.value.firstName)
    }

    @Test
    fun `the grid shows one entity per row on medium windows and two on expanded ones`() {
        assertEquals(1, homeGridColumns(WindowWidthClass.COMPACT))
        assertEquals(2, homeGridColumns(WindowWidthClass.MEDIUM))
        assertEquals(4, homeGridColumns(WindowWidthClass.EXPANDED))
    }

    @Test
    fun `home reminds to make a backup until one is saved`() = runTest {
        val viewModel = viewModel()
        advanceUntilIdle()
        assertEquals(BackupReminder.Due(null), viewModel.state.value.backupReminder)

        backups.markBackupSaved(now)
        advanceUntilIdle()
        assertEquals(BackupReminder.NoReminder, viewModel.state.value.backupReminder)

        val effects = mutableListOf<HomeEffect>()
        val job = launch { viewModel.effect.toList(effects) }
        viewModel.onIntent(HomeIntent.BackupReminderClicked)
        advanceUntilIdle()
        job.cancel()
        assertEquals(listOf<HomeEffect>(HomeEffect.OpenBackup), effects)
    }
}
