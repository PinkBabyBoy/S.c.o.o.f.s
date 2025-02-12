package ru.barinov.file_browser

import androidx.paging.PagingData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.barinov.core.FileEntity
import ru.barinov.core.FileId
import ru.barinov.core.FileTypeInfo
import ru.barinov.file_browser.models.FileUiModel
import ru.barinov.file_browser.models.ViewableFileModel

interface ViewableFileMapper <T: ViewableFileModel> {

    fun clear()

    operator fun invoke(
        files: PagingData<FileEntity>,
        selected: HashSet<FileId>,
        recognizerOn: Boolean,
    ): PagingData<T>

    fun mapFile(
        file: FileEntity,
        isSelected: Boolean,
        typeState: StateFlow<FileTypeInfo> = MutableStateFlow(FileTypeInfo.Unconfirmed).asStateFlow(),
    ): T
}