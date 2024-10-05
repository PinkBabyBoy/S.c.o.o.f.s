package ru.barinov.file_browser.states

import android.graphics.Bitmap

data class ImageFileScreenUiState internal constructor(
    val bitmapToShow: Bitmap?
) {

    companion object{
        fun idle() = ImageFileScreenUiState(null)
    }

}
