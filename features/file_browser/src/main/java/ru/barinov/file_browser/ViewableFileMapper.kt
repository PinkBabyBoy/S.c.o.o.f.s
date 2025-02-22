package ru.barinov.file_browser

import androidx.paging.PagingData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.barinov.core.FileId
import ru.barinov.core.FileTypeInfo
import ru.barinov.core.StorageAble
import ru.barinov.file_browser.models.ViewableFileModel

interface ViewableFileMapper <In: StorageAble, Out: ViewableFileModel> {

    fun clear()

    operator fun invoke(
        files: PagingData<In>,
        selected: HashSet<FileId>,
        recognizerOn: Boolean,
    ): PagingData<Out>

    fun mapFile(
        file: In,
        isSelected: Boolean,
        typeState: StateFlow<FileTypeInfo> = MutableStateFlow(FileTypeInfo.Unconfirmed).asStateFlow(),
    ): Out
}