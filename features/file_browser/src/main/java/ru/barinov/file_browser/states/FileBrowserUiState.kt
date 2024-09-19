package ru.barinov.file_browser.states

import androidx.compose.runtime.Stable
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import ru.barinov.core.Filepath
import ru.barinov.core.Source
import ru.barinov.core.folderName
import ru.barinov.file_browser.models.FileUiModel
import ru.barinov.file_browser.models.SourceState

@Stable
data class FileBrowserUiState internal constructor(
    val type: Type,
    val files: Flow<PagingData<FileUiModel>>,
    val currentFolderName: String,
    val sourceState: SourceState,
    val hasSelected: Boolean,
    val isInRoot: Boolean,
    val isPageEmpty: Boolean
) {
    val isKeyLoaded = type == Type.LOADED

    enum class Type {
        LOADED, KEY_NOT_LOADED, IDLE
    }

    companion object {
        fun reconstruct(
            files: Flow<PagingData<FileUiModel>>,
            folderName: Filepath,
            sourceState: SourceState,
            hasSelected: Boolean,
            isInRoot: Boolean,
            isKeyLoaded: Boolean,
            isPageEmpty: Boolean
        ) = FileBrowserUiState(
            type = if(isKeyLoaded) Type.LOADED else Type.KEY_NOT_LOADED,
            files = files,
            currentFolderName = folderName.value.folderName(),
            sourceState = sourceState,
            hasSelected = hasSelected,
            isInRoot = isInRoot,
            isPageEmpty = isPageEmpty
        )

        fun idle(): FileBrowserUiState =
            FileBrowserUiState(
                type = Type.IDLE,
                files = flowOf(PagingData.empty()),
                currentFolderName = String(),
                sourceState = SourceState(false, Source.INTERNAL),
                hasSelected = false,
                isInRoot = true,
                true
            )
    }
}
