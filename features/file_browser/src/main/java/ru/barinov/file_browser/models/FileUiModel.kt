package ru.barinov.file_browser.models

import android.graphics.Bitmap
import androidx.annotation.DrawableRes
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.MutableStateFlow
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
    @DrawableRes val placeholderRes: Int,
    val isSelected: Boolean,
    @Stable
    val info: MutableState<FileInfo>
)

sealed interface FileInfo {
    data object Unconfirmed : FileInfo
    class Other(val bigFile: Boolean, val size: String) : FileInfo
    class ImageFile(val bitmapPreview: Bitmap, val size: String) : FileInfo
    class Dir(val contentText: String, val count: Int) : FileInfo
    class Index(val creationDate: String) : FileInfo
}
