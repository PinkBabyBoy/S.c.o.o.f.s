package ru.barinov.file_browser.sideEffects


sealed interface BottomSheetSideEffects

sealed interface FilesLoadInitializationSideEffects : SideEffect {
    data object CloseOnShortTransaction: FilesLoadInitializationSideEffects
    data object CloseOnLongTransaction: FilesLoadInitializationSideEffects
}

object DismissConfirmed: BottomSheetSideEffects, FilesLoadInitializationSideEffects
