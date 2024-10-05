package ru.barinov.file_browser.sideEffects

import androidx.annotation.StringRes
import ru.barinov.core.FileId
import ru.barinov.core.Filename
import ru.barinov.core.Source
import ru.barinov.file_browser.models.FileInfo

interface SideEffect

sealed interface FileBrowserSideEffect : SideEffect {
    class OpenImageFile(val source: Source, val fileId: FileId) : FileBrowserSideEffect
    data object ShowAddFilesDialog : FileBrowserSideEffect
}

sealed interface FilesLoadInitializationSideEffects : SideEffect {

}

sealed interface ContainersSideEffect : SideEffect {

    data object ContainerCreated: ContainersSideEffect
}

sealed interface KeySelectorSideEffect : SideEffect {

    data class AskToLoadKey(
        val name: Filename,
        val fileId: FileId
    ) : KeySelectorSideEffect
}

class ShowInfo(@StringRes val text: Int) : KeySelectorSideEffect, FileBrowserSideEffect


data object CanGoBack : FileBrowserSideEffect, KeySelectorSideEffect
