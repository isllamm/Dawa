package com.family.dawa.core.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

interface ViewIntent
interface ViewState
interface ViewEffect

abstract class MviViewModel<I : ViewIntent, S : ViewState, E : ViewEffect>(
    initialState: S
) : ViewModel() {

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<S> = _state.asStateFlow()

    private val _effect = Channel<E>(Channel.BUFFERED)
    val effect: Flow<E> = _effect.receiveAsFlow()

    fun sendIntent(intent: I) {
        viewModelScope.launch {
            handleIntent(intent)
        }
    }

    protected abstract fun handleIntent(intent: I)

    protected fun updateState(reducer: S.() -> S) {
        _state.update(reducer)
    }

    protected fun emitEffect(effect: E) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }
}
