package ru.barinov.file_browser.viewModels

import ru.barinov.file_browser.base.SideEffectViewModel
import ru.barinov.file_browser.events.CreateContainerEvents
import ru.barinov.file_browser.sideEffects.BottomSheetSideEffects

class CreateContainerViewModel: SideEffectViewModel<BottomSheetSideEffects>() {

    fun handleEvent(event: CreateContainerEvents){}
}