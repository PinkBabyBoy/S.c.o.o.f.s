package ru.barinov.file_browser.models

import android.graphics.Bitmap
import androidx.compose.runtime.Immutable
import kotlinx.coroutines.flow.StateFlow
import ru.barinov.core.FileIndex
import ru.barinov.core.FileSize
import ru.barinov.core.FileTypeInfo

@Immutable
//TODO to Data Class
class EncryptedFileIndexUiModel(
    fileSize: FileSize,
    fileName: String,
    createdTimeStamp: Long,
    val state: FileIndex.State,
    indexFileType: StateFlow<FileTypeInfo>,
    placeHolderRes: Int,
    isSelected: Boolean
) : ViewableFileModel(fileName, fileSize, placeHolderRes, isSelected, indexFileType)
