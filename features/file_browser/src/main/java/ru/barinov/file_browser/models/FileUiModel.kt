package ru.barinov.file_browser.models

import androidx.compose.runtime.Immutable
import kotlinx.coroutines.flow.StateFlow
import ru.barinov.core.FileId
import ru.barinov.core.FileSize
import ru.barinov.core.FileTypeInfo
import ru.barinov.core.Source

@Immutable
data class FileUiModel(
    override val fileId: FileId,
    val filePath: String,
    val origin: Source,
    val isDir: Boolean,
    val isFile: Boolean,
    override val name: String,
    override val fileSize: FileSize,
    override val placeholderRes: Int,
    override val isSelected: Boolean,
    override val info: StateFlow<FileTypeInfo>,
): ViewableFileModel(fileId, name, fileSize, placeholderRes, isSelected, info)


