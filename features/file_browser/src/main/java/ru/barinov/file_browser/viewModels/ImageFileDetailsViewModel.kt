package ru.barinov.file_browser.viewModels

import android.graphics.BitmapFactory
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.barinov.core.FileEntity
import ru.barinov.core.FileId
import ru.barinov.core.Source
import ru.barinov.core.inputStream
import ru.barinov.file_browser.core.FileTreeProvider
import ru.barinov.file_browser.base.SideEffectViewModel
import ru.barinov.file_browser.core.FileProvider
import ru.barinov.file_browser.sideEffects.SideEffect
import ru.barinov.file_browser.states.ImageFileScreenUiState

class ImageFileDetailsViewModel(
    fileProvider: FileProvider,
    fileId: FileId,
    source: Source
) : SideEffectViewModel<SideEffect>() {

    private val _uiState = MutableStateFlow(ImageFileScreenUiState.idle())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val fileStream = fileProvider.getFileByID(fileId, source).inputStream()
            val bitmap = BitmapFactory.decodeStream(fileStream)
            _uiState.value = uiState.value.copy(bitmapToShow = bitmap)
        }
    }


    private fun rotate90Right() {

    }

    private fun rotate90Left(){

    }
}
