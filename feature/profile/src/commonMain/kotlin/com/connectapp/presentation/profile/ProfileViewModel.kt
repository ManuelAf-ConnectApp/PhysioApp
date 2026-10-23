package com.connectapp.presentation.profile

import androidx.lifecycle.viewModelScope
import com.connectapp.core.mvi.MviViewModel
import com.connectapp.domain.usecase.LogoutUseCase
import com.connectapp.domain.usecase.ObserveCurrentAccountUseCase
import com.connectapp.domain.usecase.ObserveCurrentProfessionalUseCase
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/** Account and professional profile of the signed-in user, from the local database. */
class ProfileViewModel(
    observeCurrentAccount: ObserveCurrentAccountUseCase,
    observeCurrentProfessional: ObserveCurrentProfessionalUseCase,
    private val logoutUseCase: LogoutUseCase,
) : MviViewModel<ProfileState, ProfileIntent, ProfileEffect>(ProfileState()) {

    init {
        combine(observeCurrentAccount(), observeCurrentProfessional()) { account, professional ->
            ProfileState(isLoading = false, account = account, professional = professional)
        }
            .onEach { newState -> updateState { newState } }
            .launchIn(viewModelScope)
    }

    override fun reduce(intent: ProfileIntent) {
        when (intent) {
            ProfileIntent.BackClicked -> emitEffect(ProfileEffect.NavigateBack)
            ProfileIntent.LogoutClicked -> {
                logoutUseCase()
                emitEffect(ProfileEffect.NavigateToLogin)
            }
        }
    }
}
