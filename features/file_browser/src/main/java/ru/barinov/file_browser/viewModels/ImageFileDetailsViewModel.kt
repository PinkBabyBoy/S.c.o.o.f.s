package ru.barinov.file_browser.viewModels

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.barinov.core.FileEntity
import ru.barinov.core.FileId
import ru.barinov.core.InteractableFile
import ru.barinov.core.inputStream
import ru.barinov.file_browser.base.SideEffectViewModel
import ru.barinov.file_browser.events.ImageDetailsEvent
import ru.barinov.file_browser.sideEffects.ImageFileDetailsSideEffects
import ru.barinov.file_browser.states.ImageFileScreenUiState
import ru.barinov.file_browser.utils.FileSingleShareBus

class ImageFileDetailsViewModel(
    private val fileSingleShareBus: FileSingleShareBus<InteractableFile>,
    private val fileId: FileId
) : SideEffectViewModel<ImageFileDetailsSideEffects>() {

    private val _uiState = MutableStateFlow(ImageFileScreenUiState.idle())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val sharedFile = fileSingleShareBus.get(FileSingleShareBus.Key.IMAGE_SHARE) ?: return@launch
            if ((sharedFile as? FileEntity)?.fileId != fileId) return@launch // Double check
            val fileStream = sharedFile.inputStream()
            val bitmap = BitmapFactory.decodeStream(fileStream)
            fileStream.close()
            _uiState.value = uiState.value.copy(bitmapToShow = bitmap, imgFile = sharedFile)
        }
    }

    fun handleEven(event: ImageDetailsEvent) {
        when (event) {
            ImageDetailsEvent.RotateLeft -> rotate90Left()
            ImageDetailsEvent.RotateRight -> rotate90Right()
            ImageDetailsEvent.SaveToContainer
            -> viewModelScope.launch {
                val shareFile = _uiState.value.imgFile ?: return@launch
                fileSingleShareBus.share(FileSingleShareBus.Key.ENCRYPTION, shareFile)
                _sideEffects.send(ImageFileDetailsSideEffects.ShowAddFilesDialog)
            }
        }
    }


    private fun rotate90Right() = rotate(90f)

    private fun rotate90Left() = rotate(-90f)

    private fun rotate(value: Float) {
        val current = uiState.value.bitmapToShow!!
        val matrix = Matrix().also {
            it.postRotate(value)
        }
        val rotatedBitmap = Bitmap.createBitmap(
            current,
            0,
            0,
            current.getWidth(),
            current.getHeight(),
            matrix,
            true
        )
        _uiState.value = uiState.value.copy(bitmapToShow = rotatedBitmap)
    }
}
