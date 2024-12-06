package ru.barinov.file_browser.events

import ru.barinov.core.FileId
import ru.barinov.core.FileTypeInfo
import ru.barinov.file_browser.models.Sort
import ru.barinov.onboarding.OnBoarding

interface FieObserverEvent //root and stub

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

sealed interface FileLoadInitializationEvent: FieObserverEvent {
    data object StartProcess: FileLoadInitializationEvent
}

sealed interface ContainersEvent : FieObserverEvent {

    class ContainerCreateConfirmed(val name: String): ContainersEvent
}

data object OnBackPressed : FileBrowserEvent, KeySelectorEvent

class OnFileClicked(
    val fileId: FileId, val selectionMode: Boolean, val fileInfo: FileTypeInfo, val isDir: Boolean
) : FileBrowserEvent, KeySelectorEvent, ContainersEvent, FileLoadInitializationEvent

data object SourceChanged : FileBrowserEvent, KeySelectorEvent

class OnboardingFinished(val onBoarding: OnBoarding): FileBrowserEvent, KeySelectorEvent
