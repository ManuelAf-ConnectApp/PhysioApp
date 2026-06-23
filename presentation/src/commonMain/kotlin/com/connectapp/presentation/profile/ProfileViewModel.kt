package com.connectapp.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.connectapp.domain.usecase.GetCurrentUserUseCase
import com.connectapp.domain.usecase.GetProfessionalByEmailUseCase
import com.connectapp.domain.usecase.LogoutUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getProfessionalByEmailUseCase: GetProfessionalByEmailUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> get() = _state.asStateFlow()

    private val _effects = MutableSharedFlow<ProfileEffect>()
    val effects: SharedFlow<ProfileEffect> get() = _effects.asSharedFlow()

    init {
        loadUser()
    }

    fun onIntent(intent: ProfileIntent) {
        when (intent) {
            ProfileIntent.LoadUser -> loadUser()
            ProfileIntent.Logout -> logout()
        }
    }

    private fun loadUser() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val user = getCurrentUserUseCase()
            val professional = user?.let { getProfessionalByEmailUseCase(it.email) }
            
            _state.update {
                it.copy(
                    isLoading = false,
                    user = user,
                    professional = professional
                )
            }
        }
    }

    private fun logout() {
        viewModelScope.launch {
            logoutUseCase()
            _effects.emit(ProfileEffect.NavigateToLogin)
        }
    }
}
