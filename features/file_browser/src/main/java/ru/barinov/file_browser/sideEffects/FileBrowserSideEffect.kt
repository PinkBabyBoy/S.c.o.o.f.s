package ru.barinov.file_browser.sideEffects

import androidx.annotation.StringRes
import ru.barinov.core.FileEntity
import ru.barinov.core.FileId
import ru.barinov.core.Filename
import ru.barinov.core.InteractableFile
import ru.barinov.core.Source

interface SideEffect

sealed interface ImageFileDetailsSideEffects : SideEffect {

    class ShowAddFilesDialog(val file: InteractableFile) : ImageFileDetailsSideEffects
}

sealed interface FileBrowserSideEffect : SideEffect {
    class OpenImageFile(val fileId: FileId) : FileBrowserSideEffect
    class ShowAddFilesDialog(val selectedFiles: Collection<InteractableFile>) : FileBrowserSideEffect
}

sealed interface FilesLoadInitializationSideEffects : SideEffect {
    data object CloseOnShortTransaction: FilesLoadInitializationSideEffects
    data object CloseOnLongTransaction: FilesLoadInitializationSideEffects
}

sealed interface ContainersSideEffect : SideEffect {

    data object ContainerCreated : ContainersSideEffect
}

sealed interface KeySelectorSideEffect : SideEffect {

    data class AskToLoadKey(
        val name: Filename,
        val fileId: FileId
    ) : KeySelectorSideEffect
}

class ShowInfo(@StringRes val text: Int) : KeySelectorSideEffect, FileBrowserSideEffect


data object CanGoBack : FileBrowserSideEffect, KeySelectorSideEffect
