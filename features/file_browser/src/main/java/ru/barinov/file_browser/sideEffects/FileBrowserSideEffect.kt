package ru.barinov.file_browser.sideEffects

import androidx.annotation.StringRes
import ru.barinov.core.FileId
import ru.barinov.core.Filename
import ru.barinov.file_browser.models.FileInfo
import java.util.UUID

interface SideEffect

sealed interface FileBrowserSideEffect: SideEffect {
    class OpenFile(val info: FileInfo,val fileId: FileId): FileBrowserSideEffect
}

sealed interface ContainersSideEffect: SideEffect {}

sealed interface KeySelectorSideEffect: SideEffect {

    data class AskToLoadKey(
        val name: Filename,
        val fileId: FileId
    ): KeySelectorSideEffect
}

class ShowInfo(@StringRes val text: Int): KeySelectorSideEffect, FileBrowserSideEffect


data object CanGoBack: FileBrowserSideEffect, KeySelectorSideEffect
