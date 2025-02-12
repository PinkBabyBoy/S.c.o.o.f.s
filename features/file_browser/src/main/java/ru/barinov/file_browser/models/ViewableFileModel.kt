package ru.barinov.file_browser.models

import androidx.annotation.DrawableRes
import kotlinx.coroutines.flow.StateFlow
import ru.barinov.core.FileSize
import ru.barinov.core.FileTypeInfo

sealed class ViewableFileModel(
    open val name: String,
    open val fileSize: FileSize,
    @DrawableRes open val placeholderRes: Int,
    open val isSelected: Boolean,
    open val info: StateFlow<FileTypeInfo>
)