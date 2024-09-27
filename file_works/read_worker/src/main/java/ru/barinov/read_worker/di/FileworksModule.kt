package ru.barinov.read_worker.di

import org.koin.dsl.bind
import org.koin.dsl.module
import ru.barinov.read_worker.ReadFileWorker
import ru.barinov.read_worker.ReadFileWorkerImpl

val readWorkerModule = module {

    factory {
        ReadFileWorkerImpl(get(), get())
    } bind ReadFileWorker::class

}
