package ru.barinov.write_worker.di

import org.koin.dsl.bind
import org.koin.dsl.module
import ru.barinov.write_worker.WriteFileWorker
import ru.barinov.write_worker.WriteFileWorkerImpl

val writeWorkerModule = module {

    factory {
        WriteFileWorkerImpl(
            cipherFactory = get(),
            encryptor = get(),
            keygen = get(),
            cipherStreamsFactory = get(),
            getMSDFileSystemUseCase = get()
        )
    } bind WriteFileWorker::class
}
