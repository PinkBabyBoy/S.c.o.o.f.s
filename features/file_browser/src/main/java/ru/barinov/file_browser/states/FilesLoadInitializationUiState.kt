package ru.barinov.file_browser.states

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import ru.barinov.file_browser.models.FileUiModel

@Immutable
data class FilesLoadInitializationUiState(
    val containers: List<FileUiModel>,
    val selectedFiles: List<FileUiModel>
) {

    companion object {
        fun empty() =
            FilesLoadInitializationUiState(
                containers = emptyList(),
                selectedFiles = emptyList()
            )
    }

}
