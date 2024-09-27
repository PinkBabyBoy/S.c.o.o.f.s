package ru.barinov.file_browser

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.paging.PagingData
import androidx.paging.map
import ru.barinov.core.FileEntity
import ru.barinov.core.FileId
import ru.barinov.core.Source
import ru.barinov.core.bytesToMbSting
import ru.barinov.core.trimFileName
import ru.barinov.core.trimFilePath
import ru.barinov.file_browser.models.FileInfo
import ru.barinov.file_browser.models.FileUiModel

class FileToUiModelMapper(
    private val mimeRecognizer: MimeRecognizer,
    private val fileInfoExtractor: FileInfoExtractor
) {

    private val savedMimes = mutableMapOf<FileId, MutableState<FileInfo>>()
    private val savedContentInfo = mutableMapOf<FileId, MutableState<String>>()

    operator fun invoke(
        files: PagingData<FileEntity>,
        selected: HashSet<FileId>,
        recognizerOn: Boolean
    ): PagingData<FileUiModel> {
        return files.map {
            val hasSavedState = savedMimes.containsKey(it.fileId)
            if (recognizerOn && !hasSavedState) {
                val typeState = mutableStateOf<FileInfo>(FileInfo.Unconfirmed)
                savedMimes[it.fileId] = typeState
                mimeRecognizer(it, typeState)
            }
            if (!savedContentInfo.containsKey(it.fileId)) {
                val state = mutableStateOf("")
                fileInfoExtractor(state, it)
                savedContentInfo[it.fileId] = state
            }
            mapFile(
                file = it,
                selected = selected,
                typeState = savedMimes[it.fileId] ?: mutableStateOf(FileInfo.Unconfirmed),
                contentInfo = savedContentInfo[it.fileId] ?: mutableStateOf(""),
            )
        }
    }

    private fun mapFile(
        file: FileEntity,
        selected: HashSet<FileId>,
        typeState: MutableState<FileInfo>,
        contentInfo: MutableState<String>
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
                fileType = typeState,
                size = size,
                contentInfo = contentInfo
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
