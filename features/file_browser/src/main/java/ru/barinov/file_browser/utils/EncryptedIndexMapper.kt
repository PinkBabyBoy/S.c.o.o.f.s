package ru.barinov.file_browser.utils

import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.flow.StateFlow
import ru.barinov.core.FileId
import ru.barinov.core.FileIndex
import ru.barinov.core.FileSize
import ru.barinov.core.FileTypeInfo
import ru.barinov.file_prober.FileInfoExtractor
import ru.barinov.file_browser.ViewableFileMapper
import ru.barinov.file_browser.models.EncryptedFileIndexUiModel

//TODO complete
internal class EncryptedIndexMapper(
   private val encryptedFileInfoExtractor: FileInfoExtractor<FileIndex>
): ViewableFileMapper<FileIndex, EncryptedFileIndexUiModel> {

    override fun clear() {
        encryptedFileInfoExtractor.clear()
    }

    override fun invoke(
        files: PagingData<FileIndex>,
        selected: HashSet<FileId>,
        recognizerOn: Boolean
    ): PagingData<EncryptedFileIndexUiModel> =
        files.map { index ->
            mapFile(index, index.id in selected, encryptedFileInfoExtractor.invoke(index, recognizerOn))
        }

    override fun mapFile(
        file: FileIndex,
        isSelected: Boolean,
        typeState: StateFlow<FileTypeInfo>
    ): EncryptedFileIndexUiModel {
        return file.run {
            EncryptedFileIndexUiModel(
                fileId = id,
                fileSize = FileSize(fileSize),
                fileName = fileName,
                createdTimeStamp = indexCreationTimeStamp,
                state = state,
                indexFileType = typeState,
                placeHolderRes = ru.barinov.core.R.drawable.file,
                isSelected = isSelected
            )
        }
    }
}
