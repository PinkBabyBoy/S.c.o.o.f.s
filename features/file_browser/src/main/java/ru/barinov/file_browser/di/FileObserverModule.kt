package ru.barinov.file_browser.di

import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.barinov.file_browser.ContainersManager
import ru.barinov.file_browser.ContainersManagerImpl
import ru.barinov.file_browser.ContainersViewModel
import ru.barinov.file_browser.FileObserverViewModel
import ru.barinov.file_browser.FileRecogniser
import ru.barinov.file_browser.FileToUiModelMapper
import ru.barinov.file_browser.FileTreeProvider
import ru.barinov.file_browser.GetMSDAttachStateProvider
import ru.barinov.file_browser.IsMSDAttachedUseCase
import ru.barinov.file_browser.KeySelectorViewModel
import ru.barinov.file_browser.RootProvider
import ru.barinov.file_browser.RootProviderImpl
import ru.barinov.file_browser.SelectedCache
import ru.barinov.file_browser.usecases.CreateContainerUseCase
import ru.barinov.file_browser.usecases.CreateKeyStoreUseCase


val fileObserverModule = module {

    factory {
        FileToUiModelMapper(get(), androidContext())
    }

    factory {
        FileRecogniser()
    }

    factory {
        SelectedCache()
    }

    factory {
        FileTreeProvider(get(), get()) { androidContext().resources.getString(-1) }
    }

    factory {
        GetMSDAttachStateProvider(get())
    }

    factory {
        IsMSDAttachedUseCase(get())
    }

    factory {
        RootProviderImpl(get(), get())
    } bind RootProvider::class

    viewModel {
        FileObserverViewModel(
            selectedCache = get(),
            fileTreeProvider = get(),
            fileToUiModelMapper = get(),
            getMSDAttachStateProvider = get(),
            fileWriter = get(),
            keyManager = get()
        )
    }

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
        CreateContainerUseCase(get())
    }

    viewModel {
        ContainersViewModel(
            containersManager = get(),
            fileToUiModelMapper = get(),
            createContainerUseCase = get(),
            keyManager = get(),
        )
    }

    viewModel {
        KeySelectorViewModel(
            getMSDAttachStateProvider = get(),
            fileTreeProvider = get(),
            fileToUiModelMapper = get(),
            keyManager = get(),
            createKeyStoreUseCase = get()
        )
    }
}
