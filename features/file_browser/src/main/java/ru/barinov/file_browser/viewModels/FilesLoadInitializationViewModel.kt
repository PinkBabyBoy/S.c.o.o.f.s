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
import ru.barinov.core.FileEntity
import ru.barinov.core.FileId
import ru.barinov.core.Source
import ru.barinov.cryptography.KeyMemoryCache
import ru.barinov.cryptography.hash.HashValidator
import ru.barinov.file_browser.ContainersManager
import ru.barinov.file_browser.FileToUiModelMapper
import ru.barinov.file_browser.base.SideEffectViewModel
import ru.barinov.file_browser.sideEffects.FilesLoadInitializationSideEffects
import ru.barinov.file_browser.states.FilesLoadInitializationUiState
import ru.barinov.cryptography.hash.utils.ContainerHashExtractor
import ru.barinov.file_browser.core.FileProvider
import ru.barinov.file_browser.events.FileLoadInitializationEvent
import ru.barinov.file_browser.events.OnFileClicked
import ru.barinov.file_process_worker.WorkersManager
import ru.barinov.transaction_manager.FileWriter

class FilesLoadInitializationViewModel(
    private val initializationMode: InitializationMode,
    private val fileProvider: FileProvider,
    private val containersManager: ContainersManager,
    private val hashValidator: HashValidator,
    private val keyMemoryCache: KeyMemoryCache,
    private val containerHashExtractor: ContainerHashExtractor,
    private val fileToUiModelMapper: FileToUiModelMapper,
    private val workersManager: WorkersManager,
    private val fileWriter: FileWriter
) : SideEffectViewModel<FilesLoadInitializationSideEffects>() {

    private val _uiState = MutableStateFlow(FilesLoadInitializationUiState.empty())
    val uiState = _uiState.asStateFlow()
    private var selectedContainerId: FileId? = null

    init {
        val selectedFilesFlow = flow {
            when (initializationMode) {
                is InitializationMode.Direct
                -> emit(initializationMode.getFileEntityById())

                is InitializationMode.Selected
                -> emit(initializationMode.selectedFiles.map { fileToUiModelMapper.mapFile(it, true) })
            }
        }.flowOn(Dispatchers.IO)

        viewModelScope.launch(Dispatchers.IO) {
            val key = keyMemoryCache.getPublicKey()!!.encoded
            containersManager.indexes.map { list ->
                list.filter {
                    hashValidator.validate(
                        storedHash = containerHashExtractor.extractHash(it),
                        input = key
                    )
                }.mapIndexed { index, fileIndex ->
                    val selected = index == 0
                    fileToUiModelMapper.mapFile(fileIndex, selected).also {
                        if(selected) selectedContainerId = it.fileId
                    }
                }
            }.combine(selectedFilesFlow, ::Pair)
//                .catch {  }
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

    private fun InitializationMode.Direct.getFileEntityById() =
    listOf(fileToUiModelMapper.mapFile(fileProvider.getFileByID(fileId, source) as FileEntity, true))

    fun onEvent(event: FileLoadInitializationEvent) {
        when(event){
            is OnFileClicked -> {
                selectedContainerId = event.fileId
                _uiState.value = uiState.value.selectContainer(event.fileId)
            }
            FileLoadInitializationEvent.StartProcess -> startProcessing()
        }
    }

    private fun startProcessing() {
        val containerId = selectedContainerId ?: return
        val selectedFiles = initializationMode.unwrap(fileProvider)
        val container = containersManager.getContainer(containerId.value)
        fileWriter.evaluateTransaction(
            containersName = container.name,
            files = selectedFiles,
            onEvaluated = { data, isLong ->
                workersManager.startEncryptWork(data.uuid.toString(), isLong, data.totalSize)
                viewModelScope.launch { _sideEffects.send(FilesLoadInitializationSideEffects.CloseOnLongTransaction) }
            }
        )
    }

    private fun InitializationMode.unwrap(fileProvider: FileProvider): List<FileEntity> =
        when(this){
            is InitializationMode.Direct -> listOf(fileProvider.getFileByID(fileId, source) as FileEntity)
            is InitializationMode.Selected -> selectedFiles.toList()
        }
}

sealed interface InitializationMode {
    class Selected(val selectedFiles: Collection<FileEntity>) : InitializationMode
    class Direct(val fileId: FileId, val source: Source) : InitializationMode
}
