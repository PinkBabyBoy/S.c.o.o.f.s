package ru.barinov.file_browser.viewModels

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.barinov.core.R
import ru.barinov.core.Source
import ru.barinov.file_browser.base.SideEffectViewModel
import ru.barinov.file_browser.events.KeyStoreCreateEvents
import ru.barinov.file_browser.events.OnDismiss
import ru.barinov.file_browser.sideEffects.BottomSheetSideEffects
import ru.barinov.file_browser.sideEffects.DismissConfirmed
import ru.barinov.file_browser.sideEffects.ShowInfo
import ru.barinov.file_browser.usecases.CreateKeyStoreUseCase
import ru.barinov.plain_explorer.interactor.FolderDataInteractor

class KeyStoreCreateViewModel(
    private val source: Source, //args
    private val folderDataInteractor: FolderDataInteractor,
    private val createKeyStoreUseCase: CreateKeyStoreUseCase,
): SideEffectViewModel<BottomSheetSideEffects>() {


    fun handleEvent(event: KeyStoreCreateEvents){
        when(event){
            is KeyStoreCreateEvents.OnConfirmed -> {
                createKeyStore(event)
                viewModelScope.launch { _sideEffects.send(DismissConfirmed) }
            }
            OnDismiss -> viewModelScope.launch { _sideEffects.send(DismissConfirmed) }
        }
    }

    private fun createKeyStore(event: KeyStoreCreateEvents.OnConfirmed) {
        viewModelScope.launch(Dispatchers.Default) {
            val folder = folderDataInteractor.getCurrentFolder(source)
            createKeyStoreUseCase(
                folder = folder,
                password = event.pass,
                name = event.name,
                loadInstantly = event.loadOnInit
            ).fold(
                onSuccess = {
                    folderDataInteractor.update(source)
                    _sideEffects.send(DismissConfirmed)
                    _sideEffects.send(ShowInfo(R.string.keystore_create_success))
                },
                onFailure = {
                    _sideEffects.send(DismissConfirmed)
                    _sideEffects.send(ShowInfo(R.string.keystore_create_fail))
                }
            )
        }
    }
}
