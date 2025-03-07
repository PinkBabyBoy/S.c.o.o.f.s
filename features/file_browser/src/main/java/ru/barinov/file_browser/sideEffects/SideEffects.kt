package ru.barinov.file_browser.sideEffects

import androidx.annotation.StringRes
import ru.barinov.core.FileId
import ru.barinov.core.Filename
import ru.barinov.core.InteractableFile

interface SideEffect

sealed interface OpenedContainerSideEffect: SideEffect

sealed interface ImageFileDetailsSideEffects : SideEffect {

    object ShowAddFilesDialog : ImageFileDetailsSideEffects
}

sealed interface FileBrowserSideEffect : SideEffect {
    data object ShowAddFilesDialog : FileBrowserSideEffect
}

class OpenImageFile(val fileId: FileId) : FileBrowserSideEffect, OpenedContainerSideEffect



sealed interface ContainersSideEffect : SideEffect {

    data object ContainerCreated : ContainersSideEffect
}

sealed interface KeySelectorSideEffect : SideEffect {

    data class AskToLoadKey(
        val name: Filename,
        val fileId: FileId
    ) : KeySelectorSideEffect

    data object ShowKeyCreationDialog: KeySelectorSideEffect
}

class ShowInfo(@StringRes val text: Int) : KeySelectorSideEffect, FileBrowserSideEffect


data object CanGoBack : FileBrowserSideEffect, KeySelectorSideEffect, OpenedContainerSideEffect

