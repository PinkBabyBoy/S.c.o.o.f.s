package ru.barinov.file_browser.viewModels

import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.barinov.core.FileEntity
import ru.barinov.core.FileId
import ru.barinov.core.Filepath
import ru.barinov.core.Source
import ru.barinov.cryptography.KeyManager
import ru.barinov.external_data.MassStorageState
import ru.barinov.file_browser.FileToUiModelMapper
import ru.barinov.file_browser.FileTreeProvider
import ru.barinov.file_browser.FilesPagingSource
import ru.barinov.file_browser.GetMSDAttachStateProvider
import ru.barinov.file_browser.PAGE_SIZE
import ru.barinov.file_browser.SelectedCache
import ru.barinov.file_browser.base.FileWalkViewModel
import ru.barinov.file_browser.base.change
import ru.barinov.file_browser.events.FileBrowserEvent
import ru.barinov.file_browser.events.OnBackPressed
import ru.barinov.file_browser.events.OnFileClicked
import ru.barinov.file_browser.events.SourceChanged
import ru.barinov.file_browser.models.FileInfo
import ru.barinov.file_browser.models.FileUiModel
import ru.barinov.file_browser.models.SourceState
import ru.barinov.file_browser.models.Sort
import ru.barinov.file_browser.sideEffects.CanGoBack
import ru.barinov.file_browser.sideEffects.FileBrowserSideEffect
import ru.barinov.file_browser.utils.sort
import ru.barinov.file_browser.states.FileBrowserUiState
import ru.barinov.transaction_manager.FileWriter

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

    private val sortType = MutableStateFlow(Sort.Type.AS_IS)


    init {
        val sourceState = getMSDAttachStateProvider.invoke()
            .map { it is MassStorageState.Ready }
            .combine(sourceType, ::SourceState)

        val files = sourceState.flatMapLatest { sourceData ->
            if (sourceData.currentSource == Source.MASS_STORAGE && sourceData.isMsdAttached)
                fileTreeProvider.massStorageFiles
            else
                fileTreeProvider.innerFiles
        }.combine(sortType) { files, sort ->
            files?.values?.sort(sort)
        }.map { sortedFiles ->
            Pager(
                config = PagingConfig(
                    prefetchDistance = PAGE_SIZE,
                    pageSize = PAGE_SIZE,
                    enablePlaceholders = false,
                    initialLoadSize = PAGE_SIZE
                ),
                pagingSourceFactory = {
                    FilesPagingSource(sortedFiles)
                }
            ).flow.cachedIn(viewModelScope).combine(selectedCache.cacheFlow) { files, selection ->
                fileToUiModelMapper(files, selection, true, 700)
            } to sortedFiles.isNullOrEmpty()
        }

        viewModelScope.launch(Dispatchers.Default) {
            combine(sourceState, files, keyManager.isKeyLoaded, ::Triple).map {
                val (sourceData, page, isKeyLoaded) = it
                val (name, isRoot) =
                    fileTreeProvider.getCurrentFolderInfo(sourceData.currentSource)
                val (folderFiles, isPageEmpty) = page
                RawUiModel(
                    files = folderFiles,
                    currentFolderName = name,
                    sourceState = sourceData,
                    isInRoot = isRoot,
                    isKeyLoaded = isKeyLoaded,
                    isPageEmpty = isPageEmpty
                )
            }
                .combine(selectedCache.cacheFlow.map { it.count() }.stateIn(viewModelScope), ::Pair)
                .catch { }
                .collectLatest {
                    val (filesList, folderName, sourceData, isInRoot, isKeyLoaded, isPageEmpty) = it.first
                    _uiState.value = FileBrowserUiState.reconstruct(
                        files = filesList,
                        folderName = folderName,
                        sourceState = sourceData,
                        selectedCount = it.second,
                        isInRoot = isInRoot,
                        isKeyLoaded = isKeyLoaded,
                        isPageEmpty = isPageEmpty,
                        selectedSortType = sortType.value
                    )
                }
        }
    }

    fun onNewEvent(event: FileBrowserEvent) {
        when (event) {
            is OnFileClicked -> onFileClicked(event.fileId, event.selectionMode, event.fileInfo)
            is FileBrowserEvent.RemoveSelection -> selectedCache.removeAll()
            OnBackPressed -> goBack()
            FileBrowserEvent.AddSelection -> askTransactionWithSelected()
            SourceChanged -> changeSource()
            FileBrowserEvent.DeleteSelected -> deleteSelected()
            is FileBrowserEvent.SortSelected -> sortType.value = event.type
        }
    }

    private fun deleteSelected() {
        viewModelScope.launch(Dispatchers.Default) {
            selectedCache.getCache().values.forEach {
                when (it) {
                    is FileEntity.InternalFile -> it.attachedOrigin.delete()
                    is FileEntity.MassStorageFile -> it.attachedOrigin.delete()
                    is FileEntity.Index -> TODO()
                }
            }
            fileTreeProvider.update(sourceType.value)
        }
    }


    private fun onSelect(fileId: FileId, selected: Boolean) {
        viewModelScope.launch {
            val file = fileTreeProvider.getFileByID(fileId, sourceType.value)
            if (!selected) {
                selectedCache.add(fileId, file as FileEntity)
            } else {
                selectedCache.remove(fileId)
            }
        }
    }

    private fun changeSource() {
        selectedCache.removeAll()
        sourceType.value = sourceType.value.change()
    }

    private fun onFileClicked(fileId: FileId, toggleMode: Boolean, info: FileInfo) {
        if (toggleMode) {
            onSelect(fileId, selectedCache.hasSelected(fileId))
            return
        }
        openFolder(fileId, info)
    }

    private fun goBack() {
        goBack {
            _sideEffects.send(CanGoBack)
        }
    }


    private fun openFolder(fileId: FileId, info: FileInfo) {
        when {
            info.isViewAble() -> {
                viewModelScope.launch{
                    _sideEffects.send(FileBrowserSideEffect.OpenFile(info, fileId))
                }
            }
            else -> fileTreeProvider.open(fileId, sourceType.value)
        }
    }

    private fun directFolderAdd(fileId: FileId) {
        val file = fileTreeProvider.getFileByID(fileId, sourceType.value)
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
    val isInRoot: Boolean,
    val isKeyLoaded: Boolean,
    val isPageEmpty: Boolean
)

private fun FileInfo.isViewAble(): Boolean =
    when (this) {
        is FileInfo.ImageFile -> true
        is FileInfo.Other -> false
        is FileInfo.Dir -> false
        is FileInfo.Index -> false
        is FileInfo.Unconfirmed -> false
    }
