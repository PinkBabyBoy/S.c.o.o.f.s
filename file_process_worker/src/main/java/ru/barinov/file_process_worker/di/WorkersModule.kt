package ru.barinov.file_process_worker.di

import org.koin.dsl.bind
import org.koin.dsl.module
import ru.barinov.file_process_worker.WorkersManager
import ru.barinov.file_process_worker.WorkersManagerImpl

val workersModule = module {
    single { WorkersManagerImpl() } bind WorkersManager::class
}
