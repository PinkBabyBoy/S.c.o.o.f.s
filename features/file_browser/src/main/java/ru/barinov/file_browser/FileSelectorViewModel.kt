package ru.barinov.file_browser

import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.barinov.core.Filepath
import ru.barinov.core.Source
import ru.barinov.cryptography.KeyManager
import ru.barinov.external_data.MassStorageState
import ru.barinov.file_browser.base.FileWalkViewModel
import ru.barinov.file_browser.base.change
import ru.barinov.file_browser.events.FileBrowserEvent
import ru.barinov.file_browser.events.OnBackPressed
import ru.barinov.file_browser.events.OnFileClicked
import ru.barinov.file_browser.events.SourceChanged
import ru.barinov.file_browser.models.FileUiModel
import ru.barinov.file_browser.models.SourceState
import ru.barinov.file_browser.sideEffects.CanGoBack
import ru.barinov.file_browser.sideEffects.FileBrowserSideEffect
import ru.barinov.file_browser.states.FileBrowserUiState
import ru.barinov.transaction_manager.FileWriter
import java.util.UUID

@Suppress("OPT_IN_USAGE")
class FileObserverViewModel(
    private val selectedCache: SelectedCache,
    fileTreeProvider: FileTreeProvider,
    private val fileToUiModelMapper: FileToUiModelMapper,
    getMSDAttachStateProvider: GetMSDAttachStateProvider,
    fileWriter: FileWriter,
    keyManager: KeyManager
) : FileWalkViewModel<FileBrowserSideEffect>(fileTreeProvider, getMSDAttachStateProvider, false) {

    private var attemptsToOpenFile = 0


    private val _uiState: MutableStateFlow<FileBrowserUiState> =
        MutableStateFlow(FileBrowserUiState.idle())
    val uiState = _uiState.asStateFlow()


    init {
        val sourceState = getMSDAttachStateProvider.invoke()
            .map { it is MassStorageState.Ready }
            .combine(sourceType, ::SourceState)

        val files = sourceState.flatMapLatest { sourceData ->
            if (sourceData.currentSource == Source.INTERNAL)
                fileTreeProvider.innerFiles
            else
                fileTreeProvider.massStorageFiles
        }.map {
            Pager(
                config = PagingConfig(
                    pageSize = PAGE_SIZE,
                    enablePlaceholders = false,
                    initialLoadSize = PAGE_SIZE
                ),
                pagingSourceFactory = {
                    FilesPagingSource(it)
                }
            ).flow.combine(selectedCache.cacheFlow) { files, selected ->
                fileToUiModelMapper(files, selected, true)
            }
        }

        viewModelScope.launch(Dispatchers.Default) {
            combine(sourceState, files, keyManager.isKeyLoaded, ::Triple).map {
                    val (sourceData, page, isKeyLoaded) = it
                    val (name, isRoot) =
                        fileTreeProvider.getCurrentFolderInfo(sourceData.currentSource)
                val hasSelected = selectedCache.cacheFlow.value.isNotEmpty()
                    RawUiModel(
                        files = page,
                        currentFolderName = name,
                        sourceState = sourceData,
                        hasSelected = hasSelected,
                        isInRoot = isRoot,
                        isKeyLoaded = isKeyLoaded
                    )
                }
                .catch { }
                .collectLatest {
                    val (filesList, folderName, sourceData, hasSelected, isInRoot, isKeyLoaded) = it
                    _uiState.value = FileBrowserUiState.reconstruct(
                        files = filesList,
                        folderName = folderName,
                        sourceState = sourceData,
                        hasSelected = hasSelected,
                        isInRoot = isInRoot,
                        isKeyLoaded = isKeyLoaded
                    )
                }
        }
    }

    fun onNewEvent(event: FileBrowserEvent) {
        when(event) {
            is OnFileClicked -> onFileClicked(event.uuid, event.selectionMode)
            is FileBrowserEvent.OnSelectionModeToggled -> {
                if(!event.enabled) {
                    selectedCache.removeAll()
                }
            }

            OnBackPressed -> goBack()
            FileBrowserEvent.AddSelection -> askTransactionWithSelected()
            SourceChanged -> changeSource()
        }
    }


    private fun onSelect(uuid: UUID, selected: Boolean) {
        viewModelScope.launch {
            val file = fileTreeProvider.getFileByUUID(uuid, sourceType.value)
            if (!selected) {
                selectedCache.add(uuid, file)
            } else {
                selectedCache.remove(file.uuid)
            }
        }
    }

    private fun changeSource() {
        selectedCache.removeAll()
        sourceType.value = sourceType.value.change()
    }

    private fun onFileClicked(uuid: UUID, toggleMode: Boolean) {
        if (toggleMode) {
            onSelect(uuid, selectedCache.hasSelected(uuid))
            return
        }
        openFolder(uuid)
    }

    private fun goBack() {
        goBack {
           _sideEffects.send(CanGoBack)
        }
    }


    private fun openFolder(uuid: UUID) {
        runCatching {
            fileTreeProvider.open(uuid, sourceType.value)
        }.fold(
            onSuccess = { attemptsToOpenFile = 0 },
            onFailure = {
                if (++attemptsToOpenFile == 3) {
                    attemptsToOpenFile = 0
//                    viewModelScope.launch {
//                        _uiEvents.emit(FileObserverUiCommand.ShowCantOpenFolderWarning)
//                    }
                }
            }
        )
    }

    private fun directFolderAdd(uuid: UUID) {
        val file = fileTreeProvider.getFileByUUID(uuid, sourceType.value)
        if (!file.isDir) error("")
//        val selectedData = file.innerFiles().map { innerFile ->
//            FileObserverUiCommand.ConfirmFilesTransaction.TransactionConfirmationArgs(
//                file.uuid,
//                file.name.value,
//                innerFile.isDir,
//                file.innerFiles().sumOf { it.size.value }.bytesToMbSting()
//            )
//        }
    }

    private fun askTransactionWithSelected() {
        viewModelScope.launch {
//            val selectedData = selectedCache.getCache().values.map { file ->
//                FileObserverUiCommand.ConfirmFilesTransaction.TransactionConfirmationArgs(
//                    file.uuid,
//                    file.name.value,
//                    file.isDir,
//                    file.size.value.bytesToMbSting()
//                )
//            }
        }
    }
}

private data class RawUiModel(
    val files: Flow<PagingData<FileUiModel>>,
    val currentFolderName: Filepath,
    val sourceState: SourceState,
    val hasSelected: Boolean,
    val isInRoot: Boolean,
    val isKeyLoaded: Boolean,
)

//sealed interface FileObserverUiCommand {
//
//    data object ShowCantOpenFolderWarning : FileObserverUiCommand
//
//    data object CanGoBack : FileObserverUiCommand
//
//    class ConfirmFilesTransaction(val destinationId: Int, val args: ConfirmFilesTransactionArgs) :
//        FileObserverUiCommand {
//
//        @Parcelize
//        class TransactionConfirmationArgs(
//            val uuid: UUID,
//            val name: String,
//            val isDir: Boolean,
//            val displayableSize: String
//        ) : Parcelable
//
//        @Parcelize
//        class ConfirmFilesTransactionArgs(
//            val listForConfirm: List<TransactionConfirmationArgs>
//        ) : Parcelable
//    }
//
//    class ShowProgress(val args: ProgressArgs, val destinationId: Int) : FileObserverUiCommand {
//
//        @Parcelize
//        class ProgressArgs(
//            val uuid: UUID,
//            val totalSize: Long
//        ) : Parcelable
//    }
//}

