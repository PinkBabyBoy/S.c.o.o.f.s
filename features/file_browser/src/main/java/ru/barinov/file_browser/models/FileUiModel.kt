package ru.barinov.file_browser.models

import android.graphics.Bitmap
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.barinov.core.FileId
import ru.barinov.core.FileSize
import ru.barinov.core.FileTypeInfo
import ru.barinov.core.Source

@Immutable
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
    val info: StateFlow<FileTypeInfo>,
)


