package com.connectapp.core.mvi

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Base ViewModel for screens that reduce [Intent]s into [State] but never emit one-shot
 * [Effect]s (e.g. navigation, toasts) — see [MviViewModel] for those that do.
 */
abstract class StateViewModel<State, Intent>(
    initialState: State,
) : ViewModel() {

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<State> = _state.asStateFlow()

    fun onIntent(intent: Intent) {
        reduce(intent)
    }

    protected abstract fun reduce(intent: Intent)

    protected fun updateState(block: (State) -> State) {
        _state.update(block)
    }
}
