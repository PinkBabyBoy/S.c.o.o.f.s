

package ru.barinov.file_browser.states

import androidx.compose.runtime.Stable
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import ru.barinov.core.Filepath
import ru.barinov.core.SortType
import ru.barinov.core.Source
import ru.barinov.core.folderName
import ru.barinov.file_browser.models.FileUiModel
import ru.barinov.file_browser.models.SourceState
import ru.barinov.onboarding.OnboardingInfo

@Stable
data class FileBrowserUiState internal constructor(
    val type: Type,
    val files: Flow<PagingData<FileUiModel>>,
    val currentFolderName: String,
    val sourceState: SourceState,
    val selectedCount: Int,
    val isInRoot: Boolean,
    val isPageEmpty: Boolean,
    val selectedSortType: SortType,
    val fileBrowserOnboarding: OnboardingInfo,
    val appBarState: AppbarState
) {
    val isKeyLoaded = type == Type.LOADED
    val hasSelected: Boolean = selectedCount > 0
    val isInOnboarding = fileBrowserOnboarding.first != null

    fun onboardingsStateChanged(state: OnboardingInfo) = copy(
        fileBrowserOnboarding = state
    )

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
            selectedSortType: SortType,
            fileBrowserOnboarding: OnboardingInfo
        ) = FileBrowserUiState(
            type = if(isKeyLoaded) Type.LOADED else Type.KEY_NOT_LOADED,
            files = files,
            currentFolderName = folderName.value.folderName(),
            sourceState = sourceState,
            selectedCount = selectedCount,
            isInRoot = isInRoot,
            isPageEmpty = isPageEmpty,
            selectedSortType = selectedSortType,
            fileBrowserOnboarding = fileBrowserOnboarding,
            appBarState = AppbarState.Browser(
                title = folderName.value,
                selectedCount = selectedCount,
                showArrow = !isInRoot,
                sourceState = sourceState,
                isPageEmpty = isPageEmpty,
                selectedSortType = selectedSortType,
                fileBrowserOnboarding = fileBrowserOnboarding
            )
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
                selectedSortType =  SortType.AS_IS,
                fileBrowserOnboarding =  null to false,
                appBarState = AppbarState.None
            )
    }
}
