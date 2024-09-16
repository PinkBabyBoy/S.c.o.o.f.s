package ru.barinov.transaction_manager.di

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.binds
import org.koin.dsl.module
import ru.barinov.transaction_manager.AppFolderProvider
import ru.barinov.transaction_manager.Cleaner
import ru.barinov.transaction_manager.GetCurrentContainerUseCase
import ru.barinov.cryptography.KeyManager
import ru.barinov.transaction_manager.FileWriter
import ru.barinov.transaction_manager.FileWriterImpl
import java.io.File

val fileWriterModule = module {

    factory {
        GetCurrentContainerUseCase(androidContext())
    }

    factory {
        FileWriterImpl(
            getCurrentContainerUseCase = get(),
            writeFieWorker = get(),
            appFolderProvider = get()
        )
    } binds (arrayOf(FileWriter::class, Cleaner::class))

    factory {
        AppFolderProvider{
            File(androidContext().applicationInfo.dataDir)
        }
    }
}
