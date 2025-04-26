package ru.barinov.file_browser.di

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.barinov.file_browser.ContainersManager
import ru.barinov.file_browser.ContainersManagerImpl
import ru.barinov.file_browser.FileBrowserOnboarding
import ru.barinov.file_browser.GetMSDAttachStateProvider
import ru.barinov.file_browser.IndexSelectedCache
import ru.barinov.file_browser.IsMSDAttachedUseCase
import ru.barinov.file_browser.KeyPickerOnboarding
import ru.barinov.file_browser.PlaintFileToUiModelMapper
import ru.barinov.file_browser.RootProviderImpl
import ru.barinov.file_browser.SelectedCache
import ru.barinov.file_browser.ViewableFileMapper
import ru.barinov.file_browser.usecases.CreateContainerUseCase
import ru.barinov.file_browser.usecases.CreateKeyStoreUseCase
import ru.barinov.file_browser.usecases.GetCurrentKeyHashUseCase
import ru.barinov.file_browser.usecases.GetSerializableCurrentKeyHashUseCase
import ru.barinov.file_browser.usecases.OpenContainerUseCase
import ru.barinov.file_browser.utils.EncryptedIndexMapper
import ru.barinov.file_browser.utils.FileBulkSingleShareBusImpl
import ru.barinov.file_browser.utils.FileSingleShareBus
import ru.barinov.file_browser.utils.FileSingleShareBusImpl
import ru.barinov.file_browser.viewModels.ContainerContentViewModel
import ru.barinov.file_browser.viewModels.ContainersViewModel
import ru.barinov.file_browser.viewModels.CreateContainerViewModel
import ru.barinov.file_browser.viewModels.FileObserverViewModel
import ru.barinov.file_browser.viewModels.FilesEncryptionInitializationViewModel
import ru.barinov.file_browser.viewModels.ImageFileDetailsViewModel
import ru.barinov.file_browser.viewModels.KeySelectorViewModel
import ru.barinov.file_browser.viewModels.KeyStoreCreateViewModel
import ru.barinov.file_browser.viewModels.KeyStoreLoadViewModel
import ru.barinov.onboarding.OnBoardingEngine


val fileObserverModule = module {

    factory(Qualifiers.fileEntityMapper) {
        PlaintFileToUiModelMapper(get(ru.barinov.file_prober.di.Qualifiers.plaintFileInfoExtractor))
    } bind ViewableFileMapper::class

    factory(Qualifiers.fileIndexMapper) {
        EncryptedIndexMapper(get(ru.barinov.file_prober.di.Qualifiers.encryptedFileInfoExtractor))
    } bind ViewableFileMapper::class

    single {
        SelectedCache()
    }

    single {
        IndexSelectedCache()
    }

    factory {
        GetMSDAttachStateProvider(get())
    }

    factory {
        IsMSDAttachedUseCase(get())
    }

    factory {
        RootProviderImpl(get(), get())
    } bind ru.barinov.plain_explorer.RootProvider::class


    factory {
        CreateKeyStoreUseCase(get(), get())
    }

    single {
        ContainersManagerImpl(
            containerProvider = get(),
            indexesProvider = get(),
        )
    } bind ContainersManager::class

    factory {
        CreateContainerUseCase(get(), get())
    }

    factory { OpenContainerUseCase(cryptoRepository = get()) }


    factory {
        GetSerializableCurrentKeyHashUseCase(keyCache = get(), keySnapshotCreator = get())
    }

    factory {
        GetCurrentKeyHashUseCase(get(), get())
    }

    factory(Qualifiers.fbOnboardings) {
        FileBrowserOnboarding(appPreferences = get())
    } bind OnBoardingEngine::class

    factory(Qualifiers.kpOnboardings) {
        KeyPickerOnboarding(appPreferences = get())
    } bind OnBoardingEngine::class

    single(Qualifiers.singleFileBus) {
        FileSingleShareBusImpl()
    } bind FileSingleShareBus::class

    single(Qualifiers.bulkFileBus) {
        FileBulkSingleShareBusImpl()
    } bind  FileSingleShareBus::class



    viewModel { params ->
        ImageFileDetailsViewModel(
            fileSingleShareBus = get(Qualifiers.singleFileBus),
            fileId = params.get()
        )
    }

    viewModel{ params ->
        KeyStoreCreateViewModel(
            source = params.get(),
            folderDataInteractor = get(),
            createKeyStoreUseCase = get()
        )
    }

    viewModel{
        KeyStoreLoadViewModel(
            fileSingleShareBus = get(Qualifiers.singleFileBus),
            keyManager = get()
        )
    }

    viewModel {
        CreateContainerViewModel(get())
    }

    viewModel {
        FilesEncryptionInitializationViewModel(
            containersManager = get(),
            hashValidator = get(),
            keyMemoryCache = get(),
            containerHashExtractor = get(),
            fileToUiModelMapper = get(Qualifiers.fileEntityMapper),
            workersManager = get(),
            fileWriter = get(),
            selectedCache = get(),
            singleShareBus = get(Qualifiers.bulkFileBus)
        )
    }

    viewModel { params ->
        ContainerContentViewModel(
            containerName = params.get(),
            indexMapper = get(Qualifiers.fileIndexMapper),
            selectedCache = get(),
            openContainerUseCase = get()
        )
    }

    viewModel {
        ContainersViewModel(
            containersManager = get(),
            fileToUiModelMapper = get(Qualifiers.fileEntityMapper),
            createContainerUseCase = get(),
            keyManager = get(),
            workersManager = get()
        )
    }

    viewModel {
        KeySelectorViewModel(
            getMSDAttachStateProvider = get(),
            folderDataInteractor = get(),
            fileToUiModelMapper = get(Qualifiers.fileEntityMapper),
            keyManager = get(),
            keyPickerOnBoarding =  get(Qualifiers.kpOnboardings),
            fileSingleShareBus = get(Qualifiers.singleFileBus)
        )
    }

    viewModel {
        FileObserverViewModel(
            selectedCache = get(),
            folderDataInteractor = get(),
            fileToUiModelMapper = get(Qualifiers.fileEntityMapper),
            getMSDAttachStateProvider = get(),
            keyManager = get(),
            fileBrowserOnboarding = get(Qualifiers.fbOnboardings),
            bulkSingleShareBus = get(Qualifiers.bulkFileBus),
            fileSingleShareBus = get(Qualifiers.singleFileBus)
        )
    }
}
