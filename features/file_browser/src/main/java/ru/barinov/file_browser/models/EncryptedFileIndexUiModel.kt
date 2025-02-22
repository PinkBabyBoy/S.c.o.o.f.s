package ru.barinov.file_browser.models

import android.graphics.Bitmap
import androidx.compose.runtime.Immutable
import kotlinx.coroutines.flow.StateFlow
import ru.barinov.core.FileId
import ru.barinov.core.FileIndex
import ru.barinov.core.FileSize
import ru.barinov.core.FileTypeInfo

@Immutable
//TODO to Data Class
data class EncryptedFileIndexUiModel(
    override val fileId: FileId,
    override val fileSize: FileSize,
    val fileName: String,
    val createdTimeStamp: Long,
    val state: FileIndex.State,
    val indexFileType: StateFlow<FileTypeInfo>,
    val placeHolderRes: Int,
    override val isSelected: Boolean
) : ViewableFileModel(fileId, fileName, fileSize, placeHolderRes, isSelected, indexFileType)
