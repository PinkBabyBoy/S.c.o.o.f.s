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
import ru.barinov.core.InteractableFile
import ru.barinov.cryptography.KeyMemoryCache
import ru.barinov.cryptography.hash.HashValidator
import ru.barinov.file_browser.ContainersManager
import ru.barinov.file_browser.base.SideEffectViewModel
import ru.barinov.file_browser.states.FilesLoadInitializationUiState
import ru.barinov.cryptography.hash.utils.ContainerHashExtractor
import ru.barinov.file_browser.SelectedCache
import ru.barinov.file_browser.ViewableFileMapper
import ru.barinov.file_browser.events.FileLoadInitializationEvent
import ru.barinov.file_browser.events.OnFileClicked
import ru.barinov.file_browser.models.FileUiModel
import ru.barinov.file_browser.sideEffects.DismissConfirmed
import ru.barinov.file_browser.sideEffects.FilesLoadInitializationSideEffects
import ru.barinov.file_browser.utils.FileSingleShareBus
import ru.barinov.file_process_worker.WorkersManager
import ru.barinov.transaction_manager.FileWriter

class FilesLoadInitializationViewModel(
    private val containersManager: ContainersManager,
    private val hashValidator: HashValidator,
    private val keyMemoryCache: KeyMemoryCache,
    private val containerHashExtractor: ContainerHashExtractor,
    private val fileToUiModelMapper: ViewableFileMapper<FileEntity, FileUiModel>,
    private val workersManager: WorkersManager,
    private val fileWriter: FileWriter,
    private val singleShareBus: FileSingleShareBus<Collection<InteractableFile>>,
    private val selectedCache: SelectedCache //To Interface
) : SideEffectViewModel<FilesLoadInitializationSideEffects>() {

    private val _uiState = MutableStateFlow(FilesLoadInitializationUiState.empty())
    val uiState = _uiState.asStateFlow()
    private var selectedContainerId: FileId? = null

    init {
        val selectedFilesFlow = flow {
            emit(singleShareBus.get(FileSingleShareBus.Key.ENCRYPTION, true)?.map {
                fileToUiModelMapper.mapFile(it as FileEntity, true)
            }.orEmpty())
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
                        if (selected) selectedContainerId = it.fileId
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

    fun onEvent(event: FileLoadInitializationEvent) {
        when (event) {
            is OnFileClicked -> {
                selectedContainerId = event.fileId
                _uiState.value = uiState.value.selectContainer(event.fileId)
            }

            FileLoadInitializationEvent.StartProcess -> startProcessing()
            FileLoadInitializationEvent.Dismiss -> viewModelScope.launch {
                singleShareBus.clear()
                _sideEffects.send(DismissConfirmed)
            }
        }
    }

    private fun startProcessing() {
        val containerId = selectedContainerId ?: return
        viewModelScope.launch {
            val selectedFiles = singleShareBus.get(FileSingleShareBus.Key.ENCRYPTION, true) ?: return@launch
            val container = containersManager.getContainer(containerId.value)
            fileWriter.evaluateTransaction(
                containersName = container.name,
                files = selectedFiles.toList(),
                onEvaluated = { data, isLong ->
                    workersManager.startEncryptWork(data.uuid.toString(), isLong, data.totalSize)
                    selectedCache.removeAll()
                    viewModelScope.launch { _sideEffects.send(FilesLoadInitializationSideEffects.CloseOnLongTransaction) }
                }
            )
        }
    }
}

//sealed interface InitializationParams {
//    class Selected(val selectedFiles: Collection<InteractableFile>) : InitializationParams
//    class Direct(val file: InteractableFile) : InitializationParams
//}
