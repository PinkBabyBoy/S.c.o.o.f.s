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
import ru.barinov.file_browser.presentation.Sort

@Stable
data class FileBrowserUiState internal constructor(
    val type: Type,
    val files: Flow<PagingData<FileUiModel>>,
    val currentFolderName: String,
    val sourceState: SourceState,
    val selectedCount: Int,
    val isInRoot: Boolean,
    val isPageEmpty: Boolean,
    val selectedSortType: Sort.Type
) {
    val isKeyLoaded = type == Type.LOADED
    val hasSelected: Boolean = selectedCount > 0

    enum class Type {
        LOADED, KEY_NOT_LOADED, IDLE
    }

    companion object {
        fun reconstruct(
            files: Flow<PagingData<FileUiModel>>,
            folderName: Filepath,
            sourceState: SourceState,
            selectedCount: Int,
            isInRoot: Boolean,
            isKeyLoaded: Boolean,
            isPageEmpty: Boolean,
            selectedSortType: Sort.Type
        ) = FileBrowserUiState(
            type = if(isKeyLoaded) Type.LOADED else Type.KEY_NOT_LOADED,
            files = files,
            currentFolderName = folderName.value.folderName(),
            sourceState = sourceState,
            selectedCount = selectedCount,
            isInRoot = isInRoot,
            isPageEmpty = isPageEmpty,
            selectedSortType = selectedSortType
        )

        fun idle(): FileBrowserUiState =
            FileBrowserUiState(
                type = Type.IDLE,
                files = flowOf(PagingData.empty()),
                currentFolderName = String(),
                sourceState = SourceState(false, Source.INTERNAL),
                selectedCount = 0,
                isInRoot = true,
                true,
                Sort.Type.AS_IS
            )
    }
}
