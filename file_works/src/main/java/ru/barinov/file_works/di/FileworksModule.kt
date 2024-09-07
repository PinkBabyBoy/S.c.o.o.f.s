package ru.barinov.file_works.di

import org.koin.dsl.bind
import org.koin.dsl.module
import ru.barinov.filework.ReadFileWorker
import ru.barinov.file_works.ReadFileWorkerImpl
import ru.barinov.filework.WriteFileWorker
import ru.barinov.file_works.WriteFileWorkerImpl

val fileworkModule = module {


    factory {
        ReadFileWorkerImpl(get())
    } bind ReadFileWorker::class

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
