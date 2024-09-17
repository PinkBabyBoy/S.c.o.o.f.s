package ru.barinov.file_browser.sideEffects

import androidx.annotation.StringRes
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
}

class ShowInfo(@StringRes val text: Int): KeySelectorSideEffect, FileBrowserSideEffect


data object CanGoBack: FileBrowserSideEffect, KeySelectorSideEffect
