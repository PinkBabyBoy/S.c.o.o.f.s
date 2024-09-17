package ru.barinov.file_browser.models

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import androidx.annotation.DrawableRes
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import ru.barinov.core.FileEntity
import ru.barinov.core.FileSize
import ru.barinov.core.Source
import ru.barinov.core.inputStream
import ru.barinov.core.mb
import java.io.InputStream
import java.util.UUID

@Stable
data class FileUiModel(
    val uuid: UUID,
    val filePath: String,
    val type: Source,
    val isDir: Boolean,
    val isFile: Boolean,
    val name: String,
    val size: FileSize,
    val displayAbleSize: String,
    @DrawableRes val placeholderRes: Int,
    val isSelected: Boolean,
    val fileType: MutableState<FileType>
)

sealed interface FileType {
    data object Unconfirmed : FileType
    class Other(val bigFile: Boolean) : FileType
    class ImageFile(val bitmapPreview: Bitmap) : FileType
}
