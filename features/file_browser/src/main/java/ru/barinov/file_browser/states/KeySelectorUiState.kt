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
import ru.barinov.onboarding.OnboardingInfo

@Stable
data class KeySelectorUiState(
    val type: Type,
    val files: Flow<PagingData<FileUiModel>>,
    val currentFolderName: String,
    val sourceState: SourceState,
    val isInRoot: Boolean,
    val isPageEmpty: Boolean,
    val onboardings: OnboardingInfo,
    val appBarState: AppbarState
) {

    val isKeyLoaded = type == Type.LOADED

    fun onboardingsStateChanged(state: OnboardingInfo) = copy(
        onboardings = state
    )

    enum class Type {
        LOADED, UNLOADED, IDLE
    }

    companion object {

        fun reconstruct(
            isKeyLoaded: Boolean,
            files: Flow<PagingData<FileUiModel>>,
            folderName: Filepath,
            sourceState: SourceState,
            isInRoot: Boolean,
            isPageEmpty: Boolean,
            onboardings: OnboardingInfo
        ) = KeySelectorUiState(
            type = if (isKeyLoaded) Type.LOADED else Type.UNLOADED,
            files = files,
            currentFolderName = folderName.value.folderName(),
            sourceState = sourceState,
            isInRoot = isInRoot,
            isPageEmpty = isPageEmpty,
            onboardings = onboardings,
            appBarState = AppbarState.KeySelection(folderName.value, !isInRoot, onboardings, sourceState)
        )

        fun idle(): KeySelectorUiState =
            KeySelectorUiState(
                type = Type.IDLE,
                files = flowOf(PagingData.empty()),
                currentFolderName = String(),
                sourceState = SourceState(false, Source.INTERNAL),
                isInRoot = true,
                isPageEmpty = true,
                onboardings = null to false,
                appBarState = AppbarState.None
            )
    }
}
