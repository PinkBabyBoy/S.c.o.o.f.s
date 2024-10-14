package ru.barinov.file_browser.states

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import ru.barinov.core.FileId
import ru.barinov.file_browser.models.FileUiModel

@Immutable
data class FilesLoadInitializationUiState internal constructor(
    val containers: List<FileUiModel>,
    val selectedFiles: List<FileUiModel>
) {
    fun selectContainer(fileId: FileId): FilesLoadInitializationUiState =
        copy(containers = containers.map {
            if (it.fileId == fileId) it.copy(isSelected = true) else it.copy(isSelected = false)
        })

    companion object {
        fun empty() =
            FilesLoadInitializationUiState(
                containers = emptyList(),
                selectedFiles = emptyList()
            )
    }

}
