package com.aytngr.core.common.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

interface BaseIntent
interface BaseState
interface BaseEffect

abstract class BaseViewModel<Intent: BaseIntent, State: BaseState, Effect: BaseEffect>(
    initialState: State
): ViewModel() {
    private val _state = MutableStateFlow(initialState)
    val state = _state.asStateFlow()

    private val _effects = Channel<Effect>(Channel.UNLIMITED)
    val effects = _effects.receiveAsFlow()

    protected val currentState: State
        get() = state.value

    abstract fun handleIntent(intent: Intent)

    protected fun setState(reducer: State.() -> State){
        _state.value = currentState.reducer()
    }
    protected fun sendEffect(effect: Effect) {
        viewModelScope.launch{
            _effects.send(effect)
        }
    }
}