package ru.barinov.file_browser

import androidx.annotation.DrawableRes
import androidx.compose.runtime.MutableState
import androidx.paging.PagingData
import androidx.paging.map
import ru.barinov.core.FileEntity
import ru.barinov.core.FileId
import ru.barinov.core.Source
import ru.barinov.core.trimFileName
import ru.barinov.core.trimFilePath
import ru.barinov.file_browser.models.FileInfo
import ru.barinov.file_browser.models.FileUiModel
import ru.barinov.file_browser.utils.FileInfoExtractor

class FileToUiModelMapper(
    private val infoExtractor: FileInfoExtractor
) {

    operator fun invoke(
        files: PagingData<FileEntity>,
        selected: HashSet<FileId>,
        recognizerOn: Boolean,
        delay: Long = 0L
    ): PagingData<FileUiModel> {
        return files.map {
            mapFile(
                file = it,
                selected = selected,
                typeState = infoExtractor(it, recognizerOn, delay)
            )
        }
    }

    private fun mapFile(
        file: FileEntity,
        selected: HashSet<FileId>,
        typeState: MutableState<FileInfo>,
    ): FileUiModel =
        file.run {
            val isSelected = fileId in selected
            FileUiModel(
                fileId = fileId,
                filePath = file.path.value.trimFilePath(),
                origin = if (this is FileEntity.MassStorageFile) Source.MASS_STORAGE else Source.INTERNAL,
                isDir = isDir,
                isFile = !isDir,
                name = name.value.trimFileName(10),
                placeholderRes = fetchPlaceholderRes(this),
                isSelected = isSelected,
                info = typeState,
                size = size,
            )
        }


    @DrawableRes
    private fun fetchPlaceholderRes(file: FileEntity): Int {
        return when (file) {
            is FileEntity.InternalFile,
            is FileEntity.MassStorageFile
            -> if (!file.isDir) ru.barinov.core.R.drawable.file else ru.barinov.core.R.drawable.folder

            is FileEntity.Index -> ru.barinov.core.R.drawable.outline_archive_24 //FIXME
        }
    }
}
