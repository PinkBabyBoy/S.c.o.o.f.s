package ru.barinov.file_browser.events

import ru.barinov.core.FileId
import ru.barinov.core.FileTypeInfo
import ru.barinov.core.SortType

interface FieObserverEvent //root and stub

sealed interface FileBrowserEvent : FieObserverEvent {

    data object AddSelectionClicked : FileBrowserEvent

    class SortSelected(val type: SortType) : FileBrowserEvent
}

sealed interface OpenedContainerEvent: FieObserverEvent

data object DeleteSelected : FileBrowserEvent, OpenedContainerEvent

data object RemoveSelection: FileBrowserEvent, OpenedContainerEvent

sealed interface KeySelectorEvent : FieObserverEvent {

    data object KeyStoreCreateClicked: KeySelectorEvent


    data object UnbindKey: KeySelectorEvent
}

sealed interface FileLoadInitializationEvent: FieObserverEvent {
    object StartProcess: FileLoadInitializationEvent
    object Dismiss: FileLoadInitializationEvent
}

sealed interface ContainersEvent : FieObserverEvent {

    class ContainerCreateConfirmed(val name: String): ContainersEvent
    object CreateContainerRequest: ContainersEvent
}

data object OnBackPressed : FileBrowserEvent, KeySelectorEvent, OpenedContainerEvent, ContainersEvent

class OnFileClicked(
    val fileId: FileId, val selectionMode: Boolean, val fileInfo: FileTypeInfo, val isDir: Boolean
) : FileBrowserEvent, KeySelectorEvent, ContainersEvent, FileLoadInitializationEvent, OpenedContainerEvent

data object SourceChanged : FileBrowserEvent, KeySelectorEvent

data object OnboardingFinished: FileBrowserEvent, KeySelectorEvent
