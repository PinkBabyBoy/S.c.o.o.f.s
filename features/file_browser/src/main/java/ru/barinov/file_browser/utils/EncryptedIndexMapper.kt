package ru.barinov.file_browser.utils

import androidx.paging.PagingData
import kotlinx.coroutines.flow.StateFlow
import ru.barinov.core.FileEntity
import ru.barinov.core.FileId
import ru.barinov.core.FileTypeInfo
import ru.barinov.file_browser.ViewableFileMapper
import ru.barinov.file_browser.models.EncryptedFileIndexUiModel

//TODO complete
internal class EncryptedIndexMapper: ViewableFileMapper<EncryptedFileIndexUiModel> {

    override fun clear() {
        TODO("Not yet implemented")
    }

    override fun invoke(
        files: PagingData<FileEntity>,
        selected: HashSet<FileId>,
        recognizerOn: Boolean
    ): PagingData<EncryptedFileIndexUiModel> {
        TODO("Not yet implemented")
    }

    override fun mapFile(
        file: FileEntity,
        isSelected: Boolean,
        typeState: StateFlow<FileTypeInfo>
    ): EncryptedFileIndexUiModel {
        TODO("Not yet implemented")
    }
}
