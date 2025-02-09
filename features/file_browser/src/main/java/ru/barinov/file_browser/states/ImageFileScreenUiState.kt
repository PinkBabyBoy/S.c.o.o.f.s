package ru.barinov.file_browser.states

import android.graphics.Bitmap
import ru.barinov.core.InteractableFile

data class ImageFileScreenUiState internal constructor(
    val bitmapToShow: Bitmap?,
    val imgFile: InteractableFile?
) {

    companion object{
        fun idle() = ImageFileScreenUiState(null, null)
    }

}
