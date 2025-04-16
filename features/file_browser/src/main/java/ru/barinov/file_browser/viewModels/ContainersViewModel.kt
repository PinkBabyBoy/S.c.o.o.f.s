package ru.barinov.file_browser.viewModels

import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.barinov.core.FileEntity
import ru.barinov.cryptography.KeyManager
import ru.barinov.file_browser.ContainersManager
import ru.barinov.file_browser.ViewableFileMapper
import ru.barinov.plain_explorer.interactor.FilesPagingSource
import ru.barinov.plain_explorer.interactor.PAGE_SIZE
import ru.barinov.file_browser.base.SideEffectViewModel
import ru.barinov.file_browser.events.ContainersEvent
import ru.barinov.file_browser.events.OnBackPressed
import ru.barinov.file_browser.events.OnFileClicked
import ru.barinov.file_browser.models.FileUiModel
import ru.barinov.file_browser.sideEffects.CanGoBack
import ru.barinov.file_browser.sideEffects.ContainersSideEffect
import ru.barinov.file_browser.states.ContainersUiState
import ru.barinov.file_browser.usecases.CreateContainerUseCase
import ru.barinov.file_process_worker.WorkersManager

class ContainersViewModel(
    private val containersManager: ContainersManager,
    private val fileToUiModelMapper: ViewableFileMapper<FileEntity, FileUiModel>,
    private val createContainerUseCase: CreateContainerUseCase,
    private val workersManager: WorkersManager,
    keyManager: KeyManager
) : SideEffectViewModel<ContainersSideEffect>() {

    private val _uiState =
        MutableStateFlow(ContainersUiState.idle(workersManager.hasActiveWork.value))
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.Default) {
            val containersPages = containersManager.indexes.map {
                Pager(
                    config = PagingConfig(
                        pageSize = PAGE_SIZE,
                        enablePlaceholders = true,
                        initialLoadSize = PAGE_SIZE
                    ),
                    pagingSourceFactory = {
                        FilesPagingSource(it)
                    }
                ).flow.cachedIn(viewModelScope)
                    .map { fileToUiModelMapper(it, hashSetOf(), true) } to it.isEmpty()
            }
            combine(
                containersPages,
                keyManager.isKeyLoaded,
                workersManager.hasActiveWork,
                ::Triple
            ).catch {

            }.collectLatest {
                val (containersPageAndKey, isKeyLoaded, hasActiveWork) = it
                val (containers, isPageEmpty) = containersPageAndKey
                _uiState.value =
                    uiState.value.copy(
                        hasActiveWork = hasActiveWork,
                        isPageEmpty = isPageEmpty,
                        containers = containers,
                        state = if (isKeyLoaded) ContainersUiState.State.LOADED else ContainersUiState.State.KEY_UNLOADED,
                    )
            }
        }
    }

    fun handleEvent(event: ContainersEvent) {
        when (event) {
            is OnFileClicked -> {
                viewModelScope.launch {
                    _sideEffects.send(ContainersSideEffect.OpenContainerDetails(event.fileId))
                }
            }

            is ContainersEvent.ContainerCreateConfirmed ->
                viewModelScope.launch(Dispatchers.IO) {
                    createContainerUseCase(event.name)
                    _sideEffects.send(ContainersSideEffect.ContainerCreated)
                }

            ContainersEvent.CreateContainerRequest -> viewModelScope.launch(Dispatchers.IO) {
                _sideEffects.send(ContainersSideEffect.OpenContainerCreateBottomSheet)
            }

            OnBackPressed -> {
                viewModelScope.launch(Dispatchers.IO) {
                    _sideEffects.send(CanGoBack)
                }
            }
        }
    }
}
