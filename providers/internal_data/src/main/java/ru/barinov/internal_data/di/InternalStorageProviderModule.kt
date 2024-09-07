package ru.barinov.internal_data.di

import org.koin.dsl.bind
import org.koin.dsl.module
import ru.barinov.internal_data.InternalStorageProvider
import ru.barinov.internal_data.InternalStorageProviderImpl

val internalStorageProviderModule = module {

    factory {
        InternalStorageProviderImpl(get())
    } bind InternalStorageProvider::class
}
