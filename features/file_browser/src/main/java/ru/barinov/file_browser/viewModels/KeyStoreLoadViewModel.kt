package ru.barinov.file_browser.viewModels

import androidx.compose.runtime.MutableState
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import ru.barinov.core.InteractableFile
import ru.barinov.core.R
import ru.barinov.cryptography.KeyManager
import ru.barinov.file_browser.base.SideEffectViewModel
import ru.barinov.file_browser.events.LoadKeyStoreEvents
import ru.barinov.file_browser.events.OnDismiss
import ru.barinov.file_browser.sideEffects.BottomSheetSideEffects
import ru.barinov.file_browser.sideEffects.DismissConfirmed
import ru.barinov.file_browser.sideEffects.ShowInfo
import ru.barinov.file_browser.utils.FileSingleShareBus

class KeyStoreLoadViewModel(
    private val fileSingleShareBus: FileSingleShareBus<InteractableFile>,
    private val keyManager: KeyManager
) : SideEffectViewModel<BottomSheetSideEffects>() {


    fun handleEvent(event: LoadKeyStoreEvents) {
        when (event) {
            is LoadKeyStoreEvents.LoadKeyStore -> loadKeyStore(event)
            OnDismiss -> {
                viewModelScope.launch {
                    fileSingleShareBus.clear()
                    _sideEffects.send(DismissConfirmed)
                }
            }
        }
    }

    private fun loadKeyStore(event: LoadKeyStoreEvents.LoadKeyStore) {
        viewModelScope.launch {
            val file = fileSingleShareBus.get(FileSingleShareBus.Key.ENCRYPTION) ?: return@launch
            keyManager.loadKey(
                keyFile = file,
                password = event.pass,
                onSuccess = {
                    viewModelScope.launch {
                        _sideEffects.send(ShowInfo(R.string.key_loaded))
                        _sideEffects.send(DismissConfirmed)
                    }
                },
                onError = {
                    viewModelScope.launch {
                        _sideEffects.send(ShowInfo(R.string.key_load_fail))
                    }
                }
            )
        }
    }
}
