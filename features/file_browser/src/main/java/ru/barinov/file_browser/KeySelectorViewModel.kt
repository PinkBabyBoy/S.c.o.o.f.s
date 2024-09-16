package ru.barinov.file_browser

import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import ru.barinov.file_browser.events.KeySelectorEvent
import ru.barinov.file_browser.events.OnBackPressed
import ru.barinov.file_browser.events.OnFileClicked
import ru.barinov.file_browser.events.SourceChanged
import ru.barinov.file_browser.models.FileUiModel
import ru.barinov.file_browser.models.SourceState
import ru.barinov.file_browser.sideEffects.CanGoBack
import ru.barinov.file_browser.sideEffects.KeySelectorSideEffect
import ru.barinov.file_browser.states.KeyPickerUiState
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class KeySelectorViewModel(
    getMSDAttachStateProvider: GetMSDAttachStateProvider,
    fileTreeProvider: FileTreeProvider,
    private val fileToUiModelMapper: FileToUiModelMapper,
    private val keyManager: KeyManager
) : FileWalkViewModel<KeySelectorSideEffect>(fileTreeProvider, getMSDAttachStateProvider, true) {


    private val _uiState: MutableStateFlow<KeyPickerUiState> =
        MutableStateFlow(KeyPickerUiState.idle())
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
            ).flow.map { fileToUiModelMapper(it, hashSetOf(), false) } to it.isNullOrEmpty()
        }


        viewModelScope.launch(Dispatchers.Default) {
            combine(keyManager.isKeyLoaded, sourceState, files, ::Triple)
                .map {
                    val (isKeyLoaded, sourceData, page) = it
                    val (name, isRoot) =
                        fileTreeProvider.getCurrentFolderInfo(sourceData.currentSource)
                    val (folderFiles, isPageEmpty) = page
                    RawUiState(
                        isKeyLoaded = isKeyLoaded,
                        sourceState = sourceData,
                        folderName = name,
                        isInRoot = isRoot,
                        files = folderFiles,
                        isPageEmpty = isPageEmpty
                    )
                }
                .catch {}
                .collectLatest {
                    val (isKeyLoaded, sourceData, folderName, isInRoot, folderFiles, isPageEmpty) = it
                    _uiState.value =
                        KeyPickerUiState.reconstruct(
                            isKeyLoaded = isKeyLoaded,
                            files = folderFiles,
                            folderName = folderName,
                            sourceState = sourceData,
                            isInRoot = isInRoot,
                            isPageEmpty = isPageEmpty
                        )
                }
        }
    }

    fun handleEvent(event: KeySelectorEvent) {
        when (event) {
            OnBackPressed -> goBack()
            is OnFileClicked -> onFileClicked(event.uuid)
            SourceChanged -> sourceType.value = sourceType.value.change()
            is KeySelectorEvent.KeyLoadConfirmed
            -> keyManager.loadKey(
                keyFile = fileTreeProvider.getFileByUUID(event.uuid, sourceType.value),
                password = event.password,
                onSuccess = {},
                onError = {
                    viewModelScope.launch {
                        _sideEffects.send(KeySelectorSideEffect.KeyLoadFail)
                    }
                }
            )
        }
    }

    private fun onFileClicked(uuid: UUID) {
        val file = fileTreeProvider.getFileByUUID(uuid, sourceType.value)
        if (file.isDir) {
            openFolder(uuid)
        } else {
            viewModelScope.launch {
                _sideEffects.send(
                    KeySelectorSideEffect.AskToLoadKey(
                        name = file.name,
                        uuid = file.uuid
                    )
                )
            }
        }
    }

    private fun goBack() {
        goBack {
            _sideEffects.send(CanGoBack)
        }
    }

    private fun openFolder(uuid: UUID) {
        runCatching {
            fileTreeProvider.open(uuid, sourceType.value)
        }
    }

    private fun unbindKey() {
        keyManager.unbindKey()
    }
}

private data class RawUiState(
    val isKeyLoaded: Boolean,
    val sourceState: SourceState,
    val folderName: Filepath,
    val isInRoot: Boolean,
    val files: Flow<PagingData<FileUiModel>>,
    val isPageEmpty: Boolean
)
