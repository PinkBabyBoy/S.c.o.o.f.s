package ru.barinov.file_browser.events

import java.util.UUID

interface FieObserverEvent

sealed interface FileBrowserEvent: FieObserverEvent {

    class OnSelectionModeToggled(val enabled: Boolean): FileBrowserEvent

    data object AddSelection: FileBrowserEvent

}

sealed interface KeySelectorEvent: FieObserverEvent {

    class KeyLoadConfirmed(val uuid: UUID, val password: CharArray): KeySelectorEvent
}

data object OnBackPressed: FileBrowserEvent, KeySelectorEvent

class OnFileClicked(val uuid: UUID, val selectionMode: Boolean): FileBrowserEvent, KeySelectorEvent

data object SourceChanged: FileBrowserEvent, KeySelectorEvent
