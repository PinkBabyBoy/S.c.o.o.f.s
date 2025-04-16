package ru.barinov.file_browser.sideEffects

import androidx.annotation.StringRes
import ru.barinov.core.FileId
import ru.barinov.core.Filename
import ru.barinov.core.InteractableFile
import ru.barinov.core.Source

interface SideEffect

sealed interface OpenedContainerSideEffect : SideEffect

sealed interface ImageFileDetailsSideEffects : SideEffect {

    object ShowAddFilesDialog : ImageFileDetailsSideEffects
}

sealed interface FileBrowserSideEffect : SideEffect {
    data object ShowAddFilesDialog : FileBrowserSideEffect
}

class OpenImageFile(val fileId: FileId) : FileBrowserSideEffect, OpenedContainerSideEffect


sealed interface ContainersSideEffect : SideEffect {

    data object ShowCantCreateMessage : ContainersSideEffect
    data object ShowCantOpenMessage : ContainersSideEffect
    data object OpenContainerCreateBottomSheet : ContainersSideEffect
    class OpenContainerDetails(val fileId: FileId) : ContainersSideEffect
}

sealed interface KeySelectorSideEffect : SideEffect {

    class AskToLoadKey(val source: Source, val fileName: Filename) : KeySelectorSideEffect

    object ShowKeyCreationDialog : KeySelectorSideEffect
}

class ShowInfo(@StringRes val text: Int) : KeySelectorSideEffect, FileBrowserSideEffect,
    BottomSheetSideEffects


data object CanGoBack : FileBrowserSideEffect, KeySelectorSideEffect, OpenedContainerSideEffect

