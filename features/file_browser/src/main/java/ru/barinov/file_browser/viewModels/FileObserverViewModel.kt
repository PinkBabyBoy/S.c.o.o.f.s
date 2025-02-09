package ru.barinov.file_browser.viewModels

import android.util.Log
import androidx.compose.material3.ExperimentalMaterial3Api
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
import ru.barinov.core.FileTypeInfo
import ru.barinov.core.Filepath
import ru.barinov.core.InteractableFile
import ru.barinov.core.SortType
import ru.barinov.core.Source
import ru.barinov.cryptography.KeyManager
import ru.barinov.external_data.MassStorageState
import ru.barinov.file_browser.FileToUiModelMapper
import ru.barinov.plain_explorer.repository.FilesPagingSource
import ru.barinov.file_browser.GetMSDAttachStateProvider
import ru.barinov.plain_explorer.repository.PAGE_SIZE
import ru.barinov.file_browser.SelectedCache
import ru.barinov.file_browser.base.FileWalkViewModel
import ru.barinov.file_browser.base.change
import ru.barinov.file_browser.events.FileBrowserEvent
import ru.barinov.file_browser.events.OnBackPressed
import ru.barinov.file_browser.events.OnFileClicked
import ru.barinov.file_browser.events.OnboardingFinished
import ru.barinov.file_browser.events.SourceChanged
import ru.barinov.file_browser.models.FileUiModel
import ru.barinov.file_browser.models.SourceState
import ru.barinov.file_browser.sideEffects.CanGoBack
import ru.barinov.file_browser.sideEffects.FileBrowserSideEffect
import ru.barinov.file_browser.sideEffects.ShowInfo
import ru.barinov.plain_explorer.repository.sort
import ru.barinov.file_browser.states.FileBrowserUiState
import ru.barinov.file_browser.utils.FileSingleShareBus
import ru.barinov.onboarding.OnBoarding
import ru.barinov.onboarding.OnBoardingEngine
import ru.barinov.plain_explorer.FileTreeProvider

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("OPT_IN_USAGE")
class FileObserverViewModel(
    private val selectedCache: SelectedCache,
    fileTreeProvider: FileTreeProvider,
    private val fileToUiModelMapper: FileToUiModelMapper,
    getMSDAttachStateProvider: GetMSDAttachStateProvider,
    keyManager: KeyManager,
    private val fileBrowserOnboarding: OnBoardingEngine,
    private val singleShareBus: FileSingleShareBus
) : FileWalkViewModel<FileBrowserSideEffect>(fileTreeProvider, getMSDAttachStateProvider, false) {

    private val _uiState: MutableStateFlow<FileBrowserUiState> =
        MutableStateFlow(FileBrowserUiState.idle())
    val uiState = _uiState.asStateFlow()

    private val sortType = MutableStateFlow(SortType.AS_IS)


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
                        selectedSortType = sortType.value,
                        fileBrowserOnboarding = fileBrowserOnboarding.current().also {
                            Log.e("@@@", "CURR $it")
                        }
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
            is OnboardingFinished -> onOnboadingFinished(event.onBoarding)
        }
    }

    private fun onOnboadingFinished(onboarding: OnBoarding) {
       _uiState.value = uiState.value.onboardingsStateChanged(fileBrowserOnboarding.next(onboarding))
    }

    private fun deleteSelected() {
        viewModelScope.launch(Dispatchers.Default) {
            selectedCache.getCache().values.forEach {
                selectedCache.remove(it.fileId)
                when (it) {
                    is FileEntity.InternalFile -> it.attachedOrigin.delete()
                    is FileEntity.MassStorageFile -> it.attachedOrigin.delete()
                    is FileEntity.Index -> TODO()
                }
            }
            fileTreeProvider.update(sourceType.value)
        }
    }


    private fun onSelect(fileId: FileId, selected: Boolean, info: FileTypeInfo) {
        viewModelScope.launch {
            val file = fileTreeProvider.getFileByID(fileId, sourceType.value)
            if (!selected) {
                if(file.isDir && info is FileTypeInfo.Dir) {
                    _sideEffects.send(ShowInfo(ru.barinov.core.R.string.select_folder_warning))
                }
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

    private fun onFileClicked(fileId: FileId, toggleMode: Boolean, info: FileTypeInfo) {
        if (toggleMode) {
            onSelect(fileId, selectedCache.hasSelected(fileId), info)
        } else {
            openFile(fileId, info)
        }
    }

    private fun goBack() {
        goBack {
            _sideEffects.send(CanGoBack)
        }
    }


    private fun openFile(fileId: FileId, info: FileTypeInfo) {
        when(info) {
            is FileTypeInfo.Dir -> fileTreeProvider.open(fileId, sourceType.value)
            is FileTypeInfo.ImageFile -> {
                viewModelScope.launch{
                    singleShareBus.share(fileTreeProvider.getFileByID(fileId, sourceType.value))
                    _sideEffects.send(FileBrowserSideEffect.OpenImageFile(fileId))
                }
            }
            is FileTypeInfo.Index -> error("")
            is FileTypeInfo.Other -> {}
            FileTypeInfo.Unconfirmed -> {}
        }
    }

    private fun directFolderAdd(fileId: FileId) {
        val file = fileTreeProvider.getFileByID(fileId, sourceType.value)
        if (!file.isDir) error("")
    }

    private fun askTransactionWithSelected() {
        viewModelScope.launch {
            _sideEffects.send(FileBrowserSideEffect.ShowAddFilesDialog(selectedCache.getCache().values.filterIsInstance<InteractableFile>()))
        }
    }

    override fun onCleared() {
        fileTreeProvider.close()
        fileToUiModelMapper.clear()
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
