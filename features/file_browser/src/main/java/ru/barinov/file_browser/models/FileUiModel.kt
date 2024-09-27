package ru.barinov.file_browser.models

import android.graphics.Bitmap
import androidx.annotation.DrawableRes
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import ru.barinov.core.FileCategory
import ru.barinov.core.FileId
import ru.barinov.core.FileSize
import ru.barinov.core.Source

@Stable
data class FileUiModel(
    val fileId: FileId,
    val filePath: String,
    val origin: Source,
    val isDir: Boolean,
    val isFile: Boolean,
    val name: String,
    val size: FileSize,
    val sizeInMb: String,
    @DrawableRes val placeholderRes: Int,
    val isSelected: Boolean,
    val fileType: MutableState<FileType>
)

sealed interface FileType {
    data object Unconfirmed : FileType
    class Other(val bigFile: Boolean) : FileType
    class ImageFile(val bitmapPreview: Bitmap) : FileType
}
