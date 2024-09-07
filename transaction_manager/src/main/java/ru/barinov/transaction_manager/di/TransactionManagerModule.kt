package ru.barinov.transaction_manager.di

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.binds
import org.koin.dsl.module
import ru.barinov.transaction_manager.AppFolderProvider
import ru.barinov.transaction_manager.Cleaner
import ru.barinov.transaction_manager.GetCurrentContainerUseCase
import ru.barinov.transaction_manager.KeyManager
import ru.barinov.transaction_manager.TransactionManager
import ru.barinov.transaction_manager.TransactionManagerImpl
import java.io.File

val transactionManagerModule = module {

    factory {
        GetCurrentContainerUseCase(androidContext())
    }

    factory {
        TransactionManagerImpl(
            readFileWorker = get(),
            getCurrentContainerUseCase = get(),
            writeFieWorker = get(),
            keyCache = get(),
            appFolderProvider = get()
        )
    } binds (arrayOf(TransactionManager::class, KeyManager::class, Cleaner::class))

    factory {
        AppFolderProvider{
            File(androidContext().applicationInfo.dataDir)
        }
    }
}
