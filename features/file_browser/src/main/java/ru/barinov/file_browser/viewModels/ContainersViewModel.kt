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
import ru.barinov.cryptography.KeyManager
import ru.barinov.file_browser.ContainersManager
import ru.barinov.file_browser.FileToUiModelMapper
import ru.barinov.file_browser.FilesPagingSource
import ru.barinov.file_browser.PAGE_SIZE
import ru.barinov.file_browser.base.SideEffectViewModel
import ru.barinov.file_browser.events.ContainersEvent
import ru.barinov.file_browser.events.OnFileClicked
import ru.barinov.file_browser.sideEffects.ContainersSideEffect
import ru.barinov.file_browser.states.ContainersUiState
import ru.barinov.file_browser.usecases.CreateContainerUseCase

class ContainersViewModel(
    private val containersManager: ContainersManager,
    private val fileToUiModelMapper: FileToUiModelMapper,
    private val createContainerUseCase: CreateContainerUseCase,
    keyManager: KeyManager
) : SideEffectViewModel<ContainersSideEffect>() {

    private val _uiState = MutableStateFlow(ContainersUiState.idle())
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
                ).flow.cachedIn(viewModelScope).map { fileToUiModelMapper(it, hashSetOf(), false) } to it.isEmpty()
            }
            combine(containersPages, keyManager.isKeyLoaded, ::Pair).catch {

            }.collectLatest {
                val (containersPageAndKey, isKeyLoaded) = it
                val (containers, isPageEmpty) = containersPageAndKey
                _uiState.value =
                    uiState.value.copy(
                        isPageEmpty = isPageEmpty,
                        containers = containers,
                        state = if (isKeyLoaded) ContainersUiState.State.LOADED else ContainersUiState.State.KEY_UNLOADED
                    )
            }
        }
    }

    fun handleEvent(event: ContainersEvent) {
        when (event) {
            is OnFileClicked -> TODO()
            is ContainersEvent.ContainerCreateConfirmed ->
                viewModelScope.launch(Dispatchers.IO) {
                    createContainerUseCase(event.name)
                    _sideEffects.send(ContainersSideEffect.ContainerCreated)
                }
        }
    }
}
