package com.connectapp.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.connectapp.presentation.home.HomeEffect.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> get() = _state.asStateFlow()

    private val _effect = MutableSharedFlow<HomeEffect>()
    val effect: SharedFlow<HomeEffect> get() = _effect.asSharedFlow()

    private val _intents = MutableSharedFlow<HomeIntent>()
    val intents: SharedFlow<HomeIntent> get() = _intents.asSharedFlow()


    fun onIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.ItemClicked -> {
                emitEffect(NavigateToDetail(intent.item))
            }

            is HomeIntent.LoadUser -> {
                _state.update {
                    it.copy(user = intent.user)
                }
            }

            HomeIntent.EmailClicked -> {
                emitEffect(NavigateToEmail)
            }
        }
    }

    private fun emitEffect(effect: HomeEffect) {
        viewModelScope.launch {
            _effect.emit(effect)
        }
    }
}
