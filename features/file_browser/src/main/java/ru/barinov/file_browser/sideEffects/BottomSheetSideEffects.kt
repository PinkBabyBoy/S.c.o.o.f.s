package ru.barinov.file_browser.sideEffects


sealed interface BottomSheetSideEffects: SideEffect

sealed interface FilesLoadInitializationSideEffects  {
    data object CloseOnShortTransaction: FilesLoadInitializationSideEffects
    data object CloseOnLongTransaction: FilesLoadInitializationSideEffects
}

object DismissConfirmed:  FilesLoadInitializationSideEffects, BottomSheetSideEffects
