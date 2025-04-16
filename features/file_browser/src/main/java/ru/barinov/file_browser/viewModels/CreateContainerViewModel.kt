package ru.barinov.file_browser.viewModels

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.barinov.file_browser.base.SideEffectViewModel
import ru.barinov.file_browser.events.CreateContainerEvents
import ru.barinov.file_browser.events.OnDismiss
import ru.barinov.file_browser.sideEffects.BottomSheetSideEffects
import ru.barinov.file_browser.sideEffects.DismissConfirmed
import ru.barinov.file_browser.usecases.CreateContainerUseCase

class CreateContainerViewModel(
    private val createContainerUseCase: CreateContainerUseCase
) : SideEffectViewModel<BottomSheetSideEffects>() {

    fun handleEvent(event: CreateContainerEvents) {
        viewModelScope.launch {
            when (event) {
                is CreateContainerEvents.CreateContainer -> {
                    createContainerUseCase(event.fileName)
                    _sideEffects.send(DismissConfirmed)
                }

                OnDismiss -> _sideEffects.send(DismissConfirmed)
            }
        }
    }
}