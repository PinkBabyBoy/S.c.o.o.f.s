package ru.barinov.file_browser.viewModels

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.barinov.cryptography.hash.HashValidator
import ru.barinov.file_browser.ContainersManager
import ru.barinov.file_browser.FileToUiModelMapper
import ru.barinov.file_browser.SelectedCache
import ru.barinov.file_browser.base.SideEffectViewModel
import ru.barinov.file_browser.sideEffects.FilesLoadInitializationSideEffects
import ru.barinov.file_browser.states.FilesLoadInitializationUiState
import ru.barinov.file_browser.usecases.GetCurrentKeyHashUseCase
import ru.barinov.cryptography.hash.utils.ContainerHashExtractor

class FilesLoadInitializationViewModel(
    private val selectedCache: SelectedCache,
    private val containersManager: ContainersManager,
    private val hashValidator: HashValidator,
    private val getCurrentKeyHashUseCase: GetCurrentKeyHashUseCase,
    private val containerHashExtractor: ContainerHashExtractor,
    private val fileToUiModelMapper: FileToUiModelMapper
) : SideEffectViewModel<FilesLoadInitializationSideEffects>() {

    private val _uiState = MutableStateFlow(FilesLoadInitializationUiState.empty())
    val uiState = _uiState.asStateFlow()

    init {
        //TODO Paging
        viewModelScope.launch(Dispatchers.IO) {
            val currentHash = getCurrentKeyHashUseCase()
            containersManager.indexes.map { list ->
               val filtered = list.filter {
                   hashValidator.validate(
                       storedHash = currentHash,
                       input = containerHashExtractor.extractHash(it)
                    )
                }
                filtered.map { fileToUiModelMapper.mapFile(it, false) }
            }.combine(flow {
                emit(selectedCache.getCache().values.map { fileToUiModelMapper.mapFile(it, true) })
            }.flowOn(Dispatchers.IO), ::Pair).collectLatest {
                val (containers, selectedFiles) = it
                _uiState.emit(
                    FilesLoadInitializationUiState(
                        containers = containers,
                        selectedFiles = selectedFiles
                    )
                )
            }
        }
    }


}
