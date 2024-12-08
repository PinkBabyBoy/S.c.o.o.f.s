package ru.barinov.file_browser.viewModels

import ru.barinov.file_browser.base.SideEffectViewModel
import ru.barinov.file_browser.sideEffects.SideEffect
import ru.barinov.file_browser.usecases.OpenContainerUseCase

class ContainerContentViewModel(
   private val containerName: String,
   private val openContainerUseCase: OpenContainerUseCase
): SideEffectViewModel<SideEffect>() {


}