package ru.barinov.file_browser.states

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import ru.barinov.file_browser.models.FileUiModel

data class ContainersUiState(
    val isPageEmpty: Boolean,
    val containers: Flow<PagingData<FileUiModel>>,
    val state: State
) {

    val isKeyLoaded = state == State.LOADED

    enum class State {
        IDLE, KEY_UNLOADED, LOADED, FAIL
    }

    companion object {
        fun idle() =
            ContainersUiState(
                containers = flowOf(PagingData.empty()),
                state = State.IDLE,
                isPageEmpty = true
            )
    }
}
