package ru.barinov.file_browser.events

import ru.barinov.core.FileId
import ru.barinov.file_browser.presentation.Sort

interface FieObserverEvent

sealed interface FileBrowserEvent : FieObserverEvent {

    data object RemoveSelection: FileBrowserEvent

    data object AddSelection : FileBrowserEvent

    data object DeleteSelected : FileBrowserEvent

    class SortSelected(val type: Sort.Type) : FileBrowserEvent
}

sealed interface KeySelectorEvent : FieObserverEvent {

    class KeyLoadConfirmed(val fileId: FileId, val password: CharArray) : KeySelectorEvent

    class CreateKeyStoreConfirmed(
        val password: CharArray,
        val name: String,
        val loadInstantly: Boolean
    ) : KeySelectorEvent

    data object UnbindKey: KeySelectorEvent
}

sealed interface ContainersEvent : FileBrowserEvent {

}

data object OnBackPressed : FileBrowserEvent, KeySelectorEvent, ContainersEvent

class OnFileClicked(
    val fileId: FileId, val selectionMode: Boolean
) : FileBrowserEvent, KeySelectorEvent, ContainersEvent

data object SourceChanged : FileBrowserEvent, KeySelectorEvent
