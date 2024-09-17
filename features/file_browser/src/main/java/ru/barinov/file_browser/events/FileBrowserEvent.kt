package ru.barinov.file_browser.events

import ru.barinov.core.Source
import java.util.UUID

interface FieObserverEvent

sealed interface FileBrowserEvent : FieObserverEvent {

    class OnSelectionModeToggled(val enabled: Boolean) : FileBrowserEvent

    data object AddSelection : FileBrowserEvent

}

sealed interface KeySelectorEvent : FieObserverEvent {

    class KeyLoadConfirmed(val uuid: UUID, val password: CharArray) : KeySelectorEvent

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
    val uuid: UUID, val selectionMode: Boolean
) : FileBrowserEvent, KeySelectorEvent, ContainersEvent

data object SourceChanged : FileBrowserEvent, KeySelectorEvent
