package ru.barinov.file_browser.base

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import ru.barinov.core.Source
import ru.barinov.external_data.MassStorageState
import ru.barinov.file_browser.FileTreeProvider
import ru.barinov.file_browser.GetMSDAttachStateProvider
import ru.barinov.file_browser.sideEffects.FileBrowserSideEffect
import ru.barinov.file_browser.sideEffects.SideEffect

abstract class FileWalkViewModel<S: SideEffect>(
    protected val fileTreeProvider: FileTreeProvider,
    getMSDAttachStateProvider: GetMSDAttachStateProvider,
    tryLoadMsdFirst: Boolean
) : ViewModel() {

    protected val _sideEffects = Channel<S>(capacity = Channel.BUFFERED)
    val sideEffects = _sideEffects.receiveAsFlow()

    protected val sourceType: MutableStateFlow<Source> = MutableStateFlow(
        if (getMSDAttachStateProvider.invoke().value is MassStorageState.Ready && tryLoadMsdFirst)
            Source.MASS_STORAGE
        else Source.INTERNAL
    )

    protected fun goBack(onAllowed: suspend () -> Unit) {
        fileTreeProvider.exit(sourceType.value, onAllowed)
    }
}

fun Source.change(): Source =
    when(this){
        Source.INTERNAL ->  Source.MASS_STORAGE
        Source.MASS_STORAGE ->  Source.INTERNAL
    }
