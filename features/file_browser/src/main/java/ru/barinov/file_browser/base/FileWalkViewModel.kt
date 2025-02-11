package ru.barinov.file_browser.base

import kotlinx.coroutines.flow.MutableStateFlow
import ru.barinov.core.Source
import ru.barinov.external_data.MassStorageState
import ru.barinov.file_browser.GetMSDAttachStateProvider
import ru.barinov.file_browser.sideEffects.SideEffect
import ru.barinov.plain_explorer.interactor.FolderDataInteractor

abstract class FileWalkViewModel<S: SideEffect>(
    protected val folderDataInteractor: FolderDataInteractor,
    getMSDAttachStateProvider: GetMSDAttachStateProvider,
    tryLoadMsdFirst: Boolean
) : SideEffectViewModel<S>() {

    protected val sourceType: MutableStateFlow<Source> = MutableStateFlow(
        if (getMSDAttachStateProvider.invoke().value is MassStorageState.Ready && tryLoadMsdFirst)
            Source.MASS_STORAGE
        else Source.INTERNAL
    )

    protected fun goBack(onAllowed: suspend () -> Unit) {
        folderDataInteractor.openParent(sourceType.value, onAllowed)
    }
}

internal fun Source.change(): Source =
    when(this){
        Source.INTERNAL ->  Source.MASS_STORAGE
        Source.MASS_STORAGE ->  Source.INTERNAL
    }
