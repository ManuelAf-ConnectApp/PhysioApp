package com.connectapp.presentation

import com.connectapp.domain.policy.EpochClock
import com.connectapp.domain.policy.SessionPolicy
import com.connectapp.domain.usecase.ExpireIdleSessionUseCase
import com.connectapp.domain.usecase.RecordUserActivityUseCase
import com.connectapp.presentation.fake.FakeAuthRepository
import com.connectapp.presentation.fake.accountWithProfile
import com.connectapp.presentation.navigation.NavigationRoute
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MainViewModelTest {

    private var now = 1_000_000L
    private val repository = FakeAuthRepository()

    @Suppress("TestFunctionName")
    private fun MainViewModel() = MainViewModel(
        RecordUserActivityUseCase(repository),
        ExpireIdleSessionUseCase(repository, EpochClock { now }),
    )

    private fun signedInViewModel(): MainViewModel {
        repository.account.value = accountWithProfile
        repository.lastActivity = now
        return MainViewModel().apply { updateNavigationState(NavigationRoute.HomeRoute) }
    }

    @Test
    fun `activity within the idle timeout keeps the session`() {
        val viewModel = signedInViewModel()
        now += SessionPolicy.IDLE_TIMEOUT_MILLIS

        viewModel.onUserActivity()

        assertEquals(1, repository.recordActivityCalls)
        assertEquals(0, repository.logoutCalls)
        assertEquals(NavigationRoute.HomeRoute, viewModel.navState.value.route)
    }

    // "cuando la app permanece inactiva más del tiempo configurado, se exige volver a autenticarse".
    @Test
    fun `the first interaction after the idle timeout signs out instead of extending the session`() {
        val viewModel = signedInViewModel()
        now += SessionPolicy.IDLE_TIMEOUT_MILLIS + 1

        viewModel.onUserActivity()

        assertEquals(0, repository.recordActivityCalls)
        assertEquals(1, repository.logoutCalls)
        assertEquals(NavigationRoute.LoginRoute, viewModel.navState.value.route)
        assertTrue(viewModel.sessionExpiredNotice.value)
        viewModel.sessionExpiredNoticeShown()
        assertFalse(viewModel.sessionExpiredNotice.value)
    }

    @Test
    fun `the periodic check signs out an idle session`() {
        val viewModel = signedInViewModel()
        now += SessionPolicy.IDLE_TIMEOUT_MILLIS + 1

        assertTrue(viewModel.checkIdleSession())
        assertEquals(NavigationRoute.LoginRoute, viewModel.navState.value.route)
    }

    @Test
    fun `nothing happens while signed out`() {
        val viewModel = MainViewModel()

        assertFalse(viewModel.checkIdleSession())
        assertEquals(NavigationRoute.SplashRoute, viewModel.navState.value.route)
        assertFalse(viewModel.sessionExpiredNotice.value)
    }

    @Test
    fun `root routes are the entry points of each session state`() {
        assertEquals(
            setOf<NavigationRoute>(
                NavigationRoute.SplashRoute,
                NavigationRoute.LoginRoute,
                NavigationRoute.OnboardingRoute,
                NavigationRoute.HomeRoute,
            ),
            listOf(
                NavigationRoute.SplashRoute, NavigationRoute.LoginRoute, NavigationRoute.RegisterRoute,
                NavigationRoute.ForgotPasswordRoute, NavigationRoute.OnboardingRoute, NavigationRoute.HomeRoute,
                NavigationRoute.ProfileRoute, NavigationRoute.SettingsRoute,
            ).plus(NavigationRoute.homeSections).filter { it.isRoot }.toSet(),
        )
    }

    @Test
    fun `starts on the splash route`() {
        assertEquals(NavigationRoute.SplashRoute, MainViewModel().navState.value.route)
    }

    @Test
    fun `updateNavigationState updates the current route`() {
        val viewModel = MainViewModel()

        viewModel.updateNavigationState(NavigationRoute.HomeRoute)

        assertEquals(NavigationRoute.HomeRoute, viewModel.navState.value.route)
    }

    @Test
    fun `showSnackBar populates the snack bar state`() {
        val viewModel = MainViewModel()

        viewModel.showSnackBar(message = "Hello", actionLabel = "Undo")

        assertEquals("Hello", viewModel.snackBarState.value?.message)
        assertEquals("Undo", viewModel.snackBarState.value?.actionLabel)
    }

    @Test
    fun `dismissSnackBar clears the snack bar state`() {
        val viewModel = MainViewModel()
        viewModel.showSnackBar(message = "Hello")

        viewModel.dismissSnackBar()

        assertNull(viewModel.snackBarState.value)
    }

    @Test
    fun `onBack from register or forgot password returns to login`() {
        listOf(NavigationRoute.RegisterRoute, NavigationRoute.ForgotPasswordRoute).forEach { route ->
            val viewModel = MainViewModel()
            viewModel.updateNavigationState(NavigationRoute.LoginRoute)
            viewModel.updateNavigationState(route)

            viewModel.onBack()

            assertEquals(listOf<NavigationRoute>(NavigationRoute.LoginRoute), viewModel.navState.value.backStack)
        }
    }

    @Test
    fun `onBack from an authenticated screen returns to home`() {
        val viewModel = signedInViewModel()
        viewModel.updateNavigationState(NavigationRoute.ProfileRoute)

        viewModel.onBack()

        assertEquals(listOf<NavigationRoute>(NavigationRoute.HomeRoute), viewModel.navState.value.backStack)
    }

    @Test
    fun `onBack on a root screen keeps it so the system can leave the app`() {
        val viewModel = MainViewModel()
        viewModel.updateNavigationState(NavigationRoute.LoginRoute)
        viewModel.updateNavigationState(NavigationRoute.RegisterRoute)
        viewModel.onBack()
        viewModel.onBack() // Login is a root: nothing below, nothing happens (the system leaves the app).
        assertEquals(listOf<NavigationRoute>(NavigationRoute.LoginRoute), viewModel.navState.value.backStack)
    }

    @Test
    fun `root routes replace the whole back stack`() {
        val viewModel = signedInViewModel()
        viewModel.updateNavigationState(NavigationRoute.PatientSearchRoute)

        viewModel.updateNavigationState(NavigationRoute.LoginRoute)

        assertEquals(listOf<NavigationRoute>(NavigationRoute.LoginRoute), viewModel.navState.value.backStack)
    }

    @Test
    fun `a Home section is pushed on top of Home and back returns to Home`() {
        val viewModel = signedInViewModel()

        viewModel.updateNavigationState(NavigationRoute.InvoiceSearchRoute)
        assertEquals(
            listOf(NavigationRoute.HomeRoute, NavigationRoute.InvoiceSearchRoute),
            viewModel.navState.value.backStack,
        )

        viewModel.onBack()
        assertEquals(listOf<NavigationRoute>(NavigationRoute.HomeRoute), viewModel.navState.value.backStack)
    }

    @Test
    fun `switching between bottom bar tabs never grows the back stack`() {
        val viewModel = signedInViewModel()

        viewModel.updateNavigationState(NavigationRoute.ProfileRoute)
        viewModel.updateNavigationState(NavigationRoute.SettingsRoute)
        viewModel.updateNavigationState(NavigationRoute.ProfileRoute)

        assertEquals(
            listOf(NavigationRoute.HomeRoute, NavigationRoute.ProfileRoute),
            viewModel.navState.value.backStack,
        )
    }

    // Agreed 2026-10-20: iPad has no system back button, so every stacked screen has a back arrow.
    @Test
    fun `home sections show both bars a back arrow and keep Inicio selected`() {
        assertEquals(8, NavigationRoute.homeSections.size)
        NavigationRoute.homeSections.forEach { route ->
            assertTrue(route.showTopBar, "$route must show the top bar")
            assertTrue(route.showTopBarBackArrow, "$route must show the back arrow")
            assertTrue(route.showBottomBar, "$route must keep the bottom bar")
            assertFalse(route.isRoot, "$route must be stacked on Home")
            assertEquals(NavigationRoute.HomeRoute, route.bottomBarDestination)
        }
    }

    @Test
    fun `a saved professional replaces its form and back returns to Home`() {
        val viewModel = signedInViewModel()
        viewModel.updateNavigationState(NavigationRoute.ProfessionalRegisterRoute)

        viewModel.replaceCurrentRoute(NavigationRoute.ProfessionalDetailRoute("pro-1"))
        assertEquals(
            listOf(NavigationRoute.HomeRoute, NavigationRoute.ProfessionalDetailRoute("pro-1")),
            viewModel.navState.value.backStack,
        )

        viewModel.onBack()
        assertEquals(listOf<NavigationRoute>(NavigationRoute.HomeRoute), viewModel.navState.value.backStack)
    }

    @Test
    fun `detail edit and sign screens keep the bars and a back arrow`() {
        listOf(
            NavigationRoute.ProfessionalDetailRoute("p"),
            NavigationRoute.ProfessionalEditRoute("p"),
            NavigationRoute.PatientDetailRoute("p"),
            NavigationRoute.PatientEditRoute("p"),
            NavigationRoute.PatientSignRoute("p"),
            NavigationRoute.ReportDetailRoute("r"),
            NavigationRoute.ReportEditRoute("r"),
        ).forEach { route ->
            assertTrue(route.showBottomBar, "$route must keep the bottom bar")
            assertTrue(route.showTopBarBackArrow, "$route must show the back arrow")
            assertEquals(NavigationRoute.HomeRoute, route.bottomBarDestination)
        }
    }

    // D15: the security screens are stacked on Ajustes and keep its tab selected.
    @Test
    fun `settings screens keep the bars a back arrow and Ajustes selected`() {
        listOf(
            NavigationRoute.ChangePasswordRoute,
            NavigationRoute.RegenerateRecoveryCodeRoute,
            NavigationRoute.LicensesRoute,
            NavigationRoute.CreateBackupRoute,
            NavigationRoute.RestoreBackupRoute(),
        ).forEach { route ->
            assertTrue(route.showBottomBar, "$route must keep the bottom bar")
            assertTrue(route.showTopBarBackArrow, "$route must show the back arrow")
            assertFalse(route.isRoot, "$route must be stacked on Ajustes")
            assertEquals(NavigationRoute.SettingsRoute, route.bottomBarDestination)
        }
    }

    @Test
    fun `back from a settings screen returns to Ajustes`() {
        val viewModel = signedInViewModel()
        viewModel.updateNavigationState(NavigationRoute.SettingsRoute)
        viewModel.updateNavigationState(NavigationRoute.ChangePasswordRoute)

        viewModel.onBack()

        assertEquals(
            listOf(NavigationRoute.HomeRoute, NavigationRoute.SettingsRoute),
            viewModel.navState.value.backStack,
        )
    }

    @Test
    fun `bottom bar destinations have no back arrow`() {
        listOf(NavigationRoute.HomeRoute, NavigationRoute.ProfileRoute, NavigationRoute.SettingsRoute).forEach { route ->
            assertFalse(route.showTopBarBackArrow, "$route is reached from the bottom bar")
            assertEquals(route, route.bottomBarDestination)
        }
    }

    // Splash, login and onboarding show no navigation bars.
    @Test
    fun `splash login and onboarding show neither top bar nor bottom bar`() {
        listOf(
            NavigationRoute.SplashRoute,
            NavigationRoute.LoginRoute,
            NavigationRoute.OnboardingRoute,
        ).forEach { route ->
            assertFalse(route.showTopBar, "$route must not show the top bar")
            assertFalse(route.showBottomBar, "$route must not show the bottom bar")
        }
    }

    // Agreed 2026-10-19: register and forgot password have a top bar with a back arrow to Login.
    @Test
    fun `register and forgot password show a top bar with a back arrow and no bottom bar`() {
        listOf(NavigationRoute.RegisterRoute, NavigationRoute.ForgotPasswordRoute).forEach { route ->
            assertTrue(route.showTopBar, "$route must show the top bar")
            assertTrue(route.showTopBarBackArrow, "$route must show the back arrow")
            assertFalse(route.showBottomBar, "$route must not show the bottom bar")
        }
        assertFalse(NavigationRoute.HomeRoute.showTopBarBackArrow)
    }

    // Inicio, Perfil and Ajustes show the title-only top bar and bottom bar.
    @Test
    fun `authenticated routes show the top bar and the bottom bar`() {
        listOf(
            NavigationRoute.HomeRoute,
            NavigationRoute.ProfileRoute,
            NavigationRoute.SettingsRoute,
        ).forEach { route ->
            assertTrue(route.showTopBar, "$route must show the top bar")
            assertTrue(route.showBottomBar, "$route must show the bottom bar")
        }
    }

    // Plan §21.7: on a tablet without accounts the sign-in screen opens the restore outside the
    // authenticated area, and after any restore the back stack starts again.
    @Test
    fun `restoring from the sign-in screen has no bars and ends on a new back stack`() {
        val fromSignIn = NavigationRoute.RestoreBackupRoute(fromSignIn = true)
        assertFalse(fromSignIn.showBottomBar)
        assertTrue(fromSignIn.showTopBarBackArrow)
        assertTrue(NavigationRoute.BackupRestoredRoute.isRoot)
        assertFalse(NavigationRoute.BackupRestoredRoute.showBottomBar)

        val viewModel = signedInViewModel()
        viewModel.updateNavigationState(NavigationRoute.SettingsRoute)
        viewModel.updateNavigationState(NavigationRoute.RestoreBackupRoute())
        viewModel.updateNavigationState(NavigationRoute.BackupRestoredRoute)

        assertEquals(listOf<NavigationRoute>(NavigationRoute.BackupRestoredRoute), viewModel.navState.value.backStack)
    }
}
