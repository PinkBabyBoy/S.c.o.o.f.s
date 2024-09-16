package ru.barinov.file_browser.sideEffects

import ru.barinov.core.Filename
import java.util.UUID

interface SideEffect

sealed interface FileBrowserSideEffect: SideEffect {


}


sealed interface KeySelectorSideEffect: SideEffect {

    data class AskToLoadKey(
        val name: Filename,
        val uuid: UUID
    ): KeySelectorSideEffect

    data object KeyLoadFail: KeySelectorSideEffect
}

data object CanGoBack: FileBrowserSideEffect, KeySelectorSideEffect
