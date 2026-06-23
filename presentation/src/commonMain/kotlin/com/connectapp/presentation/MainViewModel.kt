package com.connectapp.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.connectapp.domain.usecase.GetCurrentUserUseCase
import com.connectapp.presentation.navigation.NavigationRoute
import com.connectapp.presentation.navigation.NavigationState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase
): ViewModel() {
    private val _navState = MutableStateFlow(NavigationState(route = NavigationRoute.SplashRoute))
    val navState: StateFlow<NavigationState> get() = _navState

    init {
        checkUserSession()
    }

    private fun checkUserSession() {
        viewModelScope.launch {
            val user = getCurrentUserUseCase()
            _navState.update { 
                it.copy(
                    route = if (user != null) NavigationRoute.HomeRoute(user) else NavigationRoute.LoginRoute,
                    user = user
                )
            }
        }
    }

    fun updateNavigationState(navRoute: NavigationRoute) {
        _navState.update { currentState ->
            val newUser = when (navRoute) {
                is NavigationRoute.HomeRoute -> navRoute.user
                is NavigationRoute.PatientRoute -> navRoute.user
                else -> currentState.user
            }
            val newInvoiceId = when (navRoute) {
                is NavigationRoute.EditInvoiceRoute -> navRoute.invoiceId
                is NavigationRoute.InvoiceRoute -> navRoute.invoiceId
                else -> currentState.invoiceId
            }
            currentState.copy(
                route = navRoute,
                user = newUser,
                invoiceId = newInvoiceId
            )
        }
    }

    fun onBack() {
        val currentRoute = navState.value.route
        val currentUser = navState.value.user

        when (currentRoute) {
            NavigationRoute.ForgotPasswordRoute, NavigationRoute.RegisterRoute -> {
                updateNavigationState(NavigationRoute.LoginRoute)
            }
            is NavigationRoute.HomeRoute -> {
                // Ya estamos en la home, no hacemos nada o salimos de la app
            }
            else -> {
                if (currentUser != null) {
                    updateNavigationState(NavigationRoute.HomeRoute(user = currentUser))
                } else {
                    updateNavigationState(NavigationRoute.LoginRoute)
                }
            }
        }
    }
}
