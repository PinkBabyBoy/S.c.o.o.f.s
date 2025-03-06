package ru.barinov.file_browser.states

import ru.barinov.core.SortType
import ru.barinov.file_browser.models.SourceState
import ru.barinov.onboarding.OnboardingInfo

sealed interface AppbarState {

    data class Browser (
        val title: String,
        val selectedCount: Int,
        val showArrow: Boolean,
        val sourceState: SourceState,
        val isPageEmpty: Boolean,
        val selectedSortType: SortType,
        val fileBrowserOnboarding: OnboardingInfo,
    ) : AppbarState {
        val hasSelected: Boolean = selectedCount > 0
    }
    data class KeySelection(
        val title: String,
        val showArrow: String,
        val keySelectionOnboarding: OnboardingInfo,
        val sourceState: SourceState,
    ) : AppbarState
    class Containers() : AppbarState
    object None : AppbarState

}
