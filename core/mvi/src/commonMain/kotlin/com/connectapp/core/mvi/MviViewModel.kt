package com.connectapp.core.mvi

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * Base ViewModel for screens that, in addition to [State]/[Intent], emit one-shot [Effect]s
 * (navigation, snackbars, toasts) over [effect].
 */
abstract class MviViewModel<State, Intent, Effect>(
    initialState: State,
) : StateViewModel<State, Intent>(initialState) {

    private val _effect = MutableSharedFlow<Effect>()
    val effect: SharedFlow<Effect> = _effect.asSharedFlow()

    protected fun emitEffect(effect: Effect) {
        viewModelScope.launch {
            _effect.emit(effect)
        }
    }
}
