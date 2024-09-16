package ru.barinov.file_browser

import androidx.annotation.DrawableRes
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.map
import ru.barinov.core.FileEntity
import ru.barinov.core.Source
import ru.barinov.core.bytesToMbSting
import ru.barinov.core.trimFileName
import ru.barinov.core.trimFilePath
import ru.barinov.file_browser.models.FileType
import ru.barinov.file_browser.models.FileUiModel
import java.util.UUID

class FileToUiModelMapper(
    private val fileRecogniser: FileRecogniser
) {

    operator fun invoke(
        files: PagingData<FileEntity>,
        selected: HashSet<UUID>,
        recognizerOn: Boolean
    ):  PagingData<FileUiModel> {
        return files.map {
            val typeState = mutableStateOf<FileType>(FileType.Unconfirmed)
            if(recognizerOn) {
                fileRecogniser(it, typeState)
            }
            mapFile(it, selected, typeState)
        }
    }

    private fun mapFile(file: FileEntity, selected: HashSet<UUID>, typeState: MutableState<FileType>): FileUiModel =
        file.run {
            FileUiModel(
                uuid = uuid,
                filePath = file.path.value.trimFilePath(),
                type = if(this is FileEntity.MassStorageFile) Source.MASS_STORAGE else Source.INTERNAL,
                isDir = isDir,
                isFile = !isDir,
                name = name.value.trimFileName(10),
                size = size,
                displayAbleSize = size.value.bytesToMbSting(),
                placeholderRes = fetchPlaceholderRes(isDir),
                isSelected = uuid in selected,
                fileType = typeState
            )
        }

    @DrawableRes
    private fun fetchPlaceholderRes(isDir: Boolean): Int {
        return if(!isDir) ru.barinov.core.R.drawable.file else ru.barinov.core.R.drawable.folder
    }
}
