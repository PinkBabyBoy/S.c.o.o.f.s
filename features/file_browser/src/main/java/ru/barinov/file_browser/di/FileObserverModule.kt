package ru.barinov.file_browser.di

import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.barinov.file_browser.viewModels.ContainerContentViewModel
import ru.barinov.file_browser.ContainersManager
import ru.barinov.file_browser.ContainersManagerImpl
import ru.barinov.file_browser.FileBrowserOnboarding
import ru.barinov.core.util.FileInfoExtractor
import ru.barinov.file_browser.viewModels.ContainersViewModel
import ru.barinov.file_browser.viewModels.FileObserverViewModel
import ru.barinov.file_browser.FileToUiModelMapper
import ru.barinov.file_browser.core.FileTreeProvider
import ru.barinov.file_browser.GetMSDAttachStateProvider
import ru.barinov.file_browser.IsMSDAttachedUseCase
import ru.barinov.file_browser.KeyPickerOnboarding
import ru.barinov.file_browser.viewModels.KeySelectorViewModel
import ru.barinov.file_browser.RootProvider
import ru.barinov.file_browser.RootProviderImpl
import ru.barinov.file_browser.SelectedCache
import ru.barinov.file_browser.core.FileProvider
import ru.barinov.file_browser.usecases.CreateContainerUseCase
import ru.barinov.file_browser.usecases.CreateKeyStoreUseCase
import ru.barinov.file_browser.usecases.GetCurrentKeyHashUseCase
import ru.barinov.file_browser.usecases.GetSerializableCurrentKeyHashUseCase
import ru.barinov.core.util.IndexTypeExtractor
import ru.barinov.file_browser.usecases.OpenContainerUseCase
import ru.barinov.file_browser.viewModels.FilesLoadInitializationViewModel
import ru.barinov.file_browser.viewModels.ImageFileDetailsViewModel
import ru.barinov.onboarding.OnBoardingEngine


val fileObserverModule = module {

    factory {
        FileToUiModelMapper(get())
    }

    factory {
        SelectedCache()
    }

    factory(qualifier = Qualifiers.nonSharedFileTreeProvider) {
        FileTreeProvider(get(), get()) { androidContext().resources.getString(-1) }
    } bind FileProvider::class

    single(qualifier = Qualifiers.sharedFileTreeProvider) {
        FileTreeProvider(get(), get()) { androidContext().resources.getString(-1) }
    } bind FileProvider::class

    factory {
        GetMSDAttachStateProvider(get())
    }

    factory {
        IsMSDAttachedUseCase(get())
    }

    factory {
        RootProviderImpl(get(), get())
    } bind RootProvider::class


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

    factory { OpenContainerUseCase(indexesProvider = get(), readFileWorker = get()) }


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


    viewModel { params ->
        ImageFileDetailsViewModel(
            fileProvider = get(Qualifiers.sharedFileTreeProvider),
            fileId = params.get(),
            source = params.get(),
        )
    }

    viewModel { parameter ->
        FilesLoadInitializationViewModel(
            fileProvider = get(Qualifiers.sharedFileTreeProvider),
            initializationMode = parameter.get(),
            containersManager = get(),
            hashValidator = get(),
            keyMemoryCache = get(),
            containerHashExtractor = get(),
            fileToUiModelMapper = get(),
            workersManager = get(),
            fileWriter = get()
        )
    }

    viewModel { params ->
        ContainerContentViewModel(params.get(), get())
    }

    viewModel {
        ContainersViewModel(
            containersManager = get(),
            fileToUiModelMapper = get(),
            createContainerUseCase = get(),
            keyManager = get(),
            workersManager = get()
        )
    }

    viewModel {
        KeySelectorViewModel(
            getMSDAttachStateProvider = get(),
            fileTreeProvider = get(Qualifiers.nonSharedFileTreeProvider),
            fileToUiModelMapper = get(),
            keyManager = get(),
            createKeyStoreUseCase = get(),
            keyPickerOnBoarding =  get(Qualifiers.kpOnboardings)
        )
    }

    viewModel {
        FileObserverViewModel(
            selectedCache = get(),
            fileTreeProvider = get(Qualifiers.sharedFileTreeProvider),
            fileToUiModelMapper = get(),
            getMSDAttachStateProvider = get(),
            keyManager = get(),
            fileBrowserOnboarding = get(Qualifiers.fbOnboardings)
        )
    }
}
