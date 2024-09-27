package ru.barinov.file_browser.base

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import ru.barinov.file_browser.sideEffects.SideEffect

//TODO to core
abstract class SideEffectViewModel<S: SideEffect>: ViewModel() {

    protected val _sideEffects = Channel<S>(capacity = Channel.BUFFERED)
    val sideEffects = _sideEffects.receiveAsFlow()

}
