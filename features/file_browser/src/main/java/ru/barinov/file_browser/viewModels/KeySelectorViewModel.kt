package ru.barinov.file_browser.viewModels

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
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
import ru.barinov.core.FileEntity
import ru.barinov.core.FileId
import ru.barinov.core.Filepath
import ru.barinov.core.R
import ru.barinov.core.Source
import ru.barinov.cryptography.KeyManager
import ru.barinov.external_data.MassStorageState
import ru.barinov.file_browser.FileToUiModelMapper
import ru.barinov.file_browser.core.FileTreeProvider
import ru.barinov.file_browser.FilesPagingSource
import ru.barinov.file_browser.GetMSDAttachStateProvider
import ru.barinov.file_browser.PAGE_SIZE
import ru.barinov.file_browser.base.FileWalkViewModel
import ru.barinov.file_browser.base.change
import ru.barinov.file_browser.events.KeySelectorEvent
import ru.barinov.file_browser.events.OnBackPressed
import ru.barinov.file_browser.events.OnFileClicked
import ru.barinov.file_browser.events.OnboardingFinished
import ru.barinov.file_browser.events.SourceChanged
import ru.barinov.file_browser.models.FileUiModel
import ru.barinov.file_browser.models.SourceState
import ru.barinov.file_browser.sideEffects.CanGoBack
import ru.barinov.file_browser.sideEffects.KeySelectorSideEffect
import ru.barinov.file_browser.sideEffects.ShowInfo
import ru.barinov.file_browser.states.KeyPickerUiState
import ru.barinov.file_browser.usecases.CreateKeyStoreUseCase
import ru.barinov.onboarding.OnBoarding
import ru.barinov.onboarding.OnBoardingEngine

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalMaterial3Api::class)
class KeySelectorViewModel(
    getMSDAttachStateProvider: GetMSDAttachStateProvider,
    fileTreeProvider: FileTreeProvider,
    private val fileToUiModelMapper: FileToUiModelMapper,
    private val keyManager: KeyManager,
    private val createKeyStoreUseCase: CreateKeyStoreUseCase,
    private val keyPickerOnBoarding: OnBoardingEngine
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
                    FilesPagingSource(it?.values?.toList())
                }
            ).flow.map { page ->
                fileToUiModelMapper(page, hashSetOf(), false)
            }.cachedIn(viewModelScope) to it.isNullOrEmpty()
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
                            isPageEmpty = isPageEmpty,
                            onboardings = keyPickerOnBoarding.getInitial()
                        )
                }
        }
    }

    fun handleEvent(event: KeySelectorEvent) {
        when (event) {
            OnBackPressed -> goBack()
            is OnFileClicked -> onFileClicked(event.fileId)
            SourceChanged -> sourceType.value = sourceType.value.change()
            is KeySelectorEvent.KeyLoadConfirmed
            -> keyManager.loadKey(
                keyFile = fileTreeProvider.getFileByID(event.fileId, sourceType.value),
                password = event.password,
                onSuccess = {
                    viewModelScope.launch {
                        _sideEffects.send(ShowInfo(R.string.key_loaded))
                    }
                },
                onError = {
                    viewModelScope.launch {
                        _sideEffects.send(ShowInfo(R.string.key_load_fail))
                    }
                }
            )

            is KeySelectorEvent.CreateKeyStoreConfirmed -> {
                viewModelScope.launch(Dispatchers.Default) {
                    val folder = fileTreeProvider.getCurrentFolder(sourceType.value)
                    createKeyStoreUseCase(
                        folder = folder,
                        password = event.password,
                        name = event.name,
                        loadInstantly = event.loadInstantly
                    ).fold(
                        onSuccess = {
                            fileTreeProvider.update(sourceType.value)
                            _sideEffects.send(ShowInfo(R.string.keystore_create_success))
                        },
                        onFailure = {
                            _sideEffects.send(ShowInfo(R.string.keystore_create_fail))
                        }
                    )
                }
            }

            KeySelectorEvent.UnbindKey -> unbindKey()
            is OnboardingFinished -> onOnboadingFinished(event.onBoarding)
        }
    }

    private fun onOnboadingFinished(onboarding: OnBoarding) {
        _uiState.value = uiState.value.onboardingsStateChanged(keyPickerOnBoarding.next(onboarding, viewModelScope))
    }

    private fun onFileClicked(fileId: FileId) {
        val file = fileTreeProvider.getFileByID(fileId, sourceType.value) as FileEntity
        if (file.isDir) {
            openFolder(fileId)
        } else {
            viewModelScope.launch {
                _sideEffects.send(
                    KeySelectorSideEffect.AskToLoadKey(
                        name = file.name,
                        fileId = file.fileId
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

    private fun openFolder(fileId: FileId) {
        runCatching {
            fileTreeProvider.open(fileId, sourceType.value)
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
