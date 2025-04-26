package ru.barinov.file_browser.viewModels


import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.barinov.core.FileIndex
import ru.barinov.file_browser.IndexSelectedCache
import ru.barinov.file_browser.SelectedCache
import ru.barinov.file_browser.ViewableFileMapper
import ru.barinov.file_browser.base.SideEffectViewModel
import ru.barinov.file_browser.events.DeleteSelected
import ru.barinov.file_browser.events.OnBackPressed
import ru.barinov.file_browser.events.OnFileClicked
import ru.barinov.file_browser.events.OpenedContainerEvent
import ru.barinov.file_browser.events.RemoveSelection
import ru.barinov.file_browser.models.EncryptedFileIndexUiModel
import ru.barinov.file_browser.sideEffects.CanGoBack
import ru.barinov.file_browser.sideEffects.OpenedContainerSideEffect
import ru.barinov.file_browser.usecases.OpenContainerUseCase

//TODO complete
class ContainerContentViewModel(
    containerName: String,
    indexMapper: ViewableFileMapper<FileIndex, EncryptedFileIndexUiModel>,
    private val selectedCache: IndexSelectedCache,
    openContainerUseCase: OpenContainerUseCase
) : SideEffectViewModel<OpenedContainerSideEffect>() {

    private val _uiState: MutableStateFlow<ContainerContentViewState> =
        MutableStateFlow(ContainerContentViewState.Loading)
    val viewState = _uiState.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            flow {
                emit(openContainerUseCase(containerName).cachedIn(viewModelScope)
                    .flowOn(Dispatchers.IO)
                    .combine(selectedCache.cacheFlow, ::Pair)
                    .map { combinedData ->
                        val (page, cache) = combinedData
                        indexMapper(page, cache, true)
                    }
                )
            }
                .catch {
                    _uiState.emit(ContainerContentViewState.Error)
                }
                .collectLatest {
                    _uiState.emit(ContainerContentViewState.ContainerLoaded(containerName, it))
                }
        }

    }

    fun handleEvent(event: OpenedContainerEvent) {
        when (event) {
            DeleteSelected -> TODO()
            OnBackPressed -> viewModelScope.launch { _sideEffects.send(CanGoBack) }
            is OnFileClicked -> if (event.selectionMode)
                if (!selectedCache.hasSelected(event.fileId))
                    selectedCache.add(event.fileId, event.model as EncryptedFileIndexUiModel)
                else selectedCache.remove(event.fileId)

            RemoveSelection -> selectedCache.removeAll()
        }
    }
}


sealed interface ContainerContentViewState {
    data object Loading : ContainerContentViewState
    data class ContainerLoaded(
        val containerName: String,
        val pageDataFlow: Flow<PagingData<EncryptedFileIndexUiModel>>
    ) : ContainerContentViewState

    data object Error : ContainerContentViewState
}
