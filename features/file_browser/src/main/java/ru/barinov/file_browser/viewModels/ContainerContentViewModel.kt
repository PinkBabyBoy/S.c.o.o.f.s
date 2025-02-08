package ru.barinov.file_browser.viewModels

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.barinov.file_browser.base.SideEffectViewModel
import ru.barinov.file_browser.sideEffects.SideEffect
import ru.barinov.file_browser.usecases.OpenContainerUseCase

class ContainerContentViewModel(
    private val containerName: String,
    private val openContainerUseCase: OpenContainerUseCase
) : SideEffectViewModel<SideEffect>() {

        val uiState = openContainerUseCase.invoke(containerName).map { it.size }

}