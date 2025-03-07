package ru.barinov.file_browser.events

import ru.barinov.file_browser.sideEffects.SideEffect

sealed interface BottomSheetEvent

sealed interface KeyStoreCreateEvents {
    class OnConfirmed(val name: String, val pass: CharArray, val loadOnInit: Boolean): KeyStoreCreateEvents
}

sealed interface LoadKeyStoreEvents{
    class LoadKeyStore(val pass: CharArray): LoadKeyStoreEvents
}
sealed interface CreateContainerEvents{
    class CreateContainer(val fileName: String): CreateContainerEvents
}


data object OnDismiss: BottomSheetEvent, KeyStoreCreateEvents, CreateContainerEvents, LoadKeyStoreEvents
