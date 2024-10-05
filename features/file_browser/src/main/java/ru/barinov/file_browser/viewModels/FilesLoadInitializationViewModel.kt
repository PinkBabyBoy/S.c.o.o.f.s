package ru.barinov.file_browser.viewModels

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.barinov.core.FileEntity
import ru.barinov.core.FileId
import ru.barinov.core.Source
import ru.barinov.cryptography.hash.HashValidator
import ru.barinov.file_browser.ContainersManager
import ru.barinov.file_browser.FileToUiModelMapper
import ru.barinov.file_browser.SelectedCache
import ru.barinov.file_browser.base.SideEffectViewModel
import ru.barinov.file_browser.sideEffects.FilesLoadInitializationSideEffects
import ru.barinov.file_browser.states.FilesLoadInitializationUiState
import ru.barinov.file_browser.usecases.GetCurrentKeyHashUseCase
import ru.barinov.cryptography.hash.utils.ContainerHashExtractor
import ru.barinov.file_browser.core.FileProvider

class FilesLoadInitializationViewModel(
    initializationMode: InitializationMode,
    fileProvider: FileProvider,
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
                if (initializationMode is InitializationMode.Direct)
                    emit(listOf(fileToUiModelMapper.mapFile(fileProvider.getFileByID(initializationMode.fileId, initializationMode.source) as FileEntity, true)))
                else  emit(selectedCache.getCache().values.map { fileToUiModelMapper.mapFile(it, true) })
            }.flowOn(Dispatchers.IO), ::Pair)
                .catch {  }
                .collectLatest {
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

sealed interface InitializationMode{
    data object Selected: InitializationMode
    class Direct(val fileId: FileId, val source: Source): InitializationMode
}
