package com.connectapp.presentation

import androidx.lifecycle.ViewModel
import com.connectapp.domain.usecase.ExpireIdleSessionUseCase
import com.connectapp.domain.usecase.RecordUserActivityUseCase
import com.connectapp.presentation.navigation.NavigationRoute
import com.connectapp.presentation.navigation.NavigationState
import com.connectapp.presentation.navigation.SnackBarState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/**
 * Shell state: current route, snackbar and the session idle timeout (D14). No analytics, no
 * notification permissions and no appearance preference — none of them are PhysioApp
 * requirements (phase 2 cleanup).
 */
class MainViewModel(
    private val recordUserActivity: RecordUserActivityUseCase,
    private val expireIdleSession: ExpireIdleSessionUseCase,
) : ViewModel() {
    private val _navState = MutableStateFlow(NavigationState(backStack = listOf(NavigationRoute.SplashRoute)))
    val navState: StateFlow<NavigationState> get() = _navState

    private val _snackBarState = MutableStateFlow<SnackBarState?>(null)
    val snackBarState: StateFlow<SnackBarState?> get() = _snackBarState

    private val _sessionExpiredNotice = MutableStateFlow(false)

    /** True after the idle timeout has signed the user out, until the notice has been shown. */
    val sessionExpiredNotice: StateFlow<Boolean> get() = _sessionExpiredNotice

    fun updateNavigationState(navRoute: NavigationRoute) {
        _navState.update { current ->
            // StateFlow updates synchronously on the UI thread. Ignoring a repeated destination
            // makes a double-tap harmless before Compose has removed the previous click target.
            if (current.route == navRoute) current else current.navigate(navRoute)
        }
    }

    fun replaceCurrentRoute(navRoute: NavigationRoute) {
        _navState.update { current ->
            if (current.route == navRoute) current else current.replaceCurrent(navRoute)
        }
    }

    fun showSnackBar(message: String, actionLabel: String? = null, onAction: (() -> Unit)? = null) {
        _snackBarState.value = SnackBarState(message, actionLabel, onAction)
    }

    fun dismissSnackBar() {
        _snackBarState.value = null
    }

    /**
     * Any touch or key press. The idle check runs first, so the first interaction after the
     * timeout signs out instead of silently extending an already expired session.
     */
    fun onUserActivity() {
        if (!checkIdleSession()) recordUserActivity()
    }

    /** Called periodically and when the app returns to the foreground. */
    fun checkIdleSession(): Boolean {
        val expired = expireIdleSession()
        if (expired) {
            _sessionExpiredNotice.value = true
            updateNavigationState(NavigationRoute.LoginRoute)
        }
        return expired
    }

    fun sessionExpiredNoticeShown() {
        _sessionExpiredNotice.value = false
    }

    /**
     * Top bar back arrow and system back: returns to the previous screen. A screen with nothing
     * below it falls back to the entry point of its area, so the arrow is never a no-op.
     */
    fun onBack() {
        val current = navState.value
        val previous = current.pop()
        when {
            previous != null -> _navState.value = previous
            current.route == NavigationRoute.ForgotPasswordRoute ||
                current.route == NavigationRoute.RegisterRoute ||
                current.route == NavigationRoute.RestoreBackupRoute(fromSignIn = true) ->
                updateNavigationState(NavigationRoute.LoginRoute)
            current.route.isAuthenticatedArea && current.route != NavigationRoute.HomeRoute ->
                updateNavigationState(NavigationRoute.HomeRoute)
        }
    }
}
