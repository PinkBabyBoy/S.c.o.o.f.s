package ru.barinov.internal_data.di

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.barinov.internal_data.ContainerProvider
import ru.barinov.internal_data.ContainerProviderImpl
import ru.barinov.internal_data.IndexesProvider
import ru.barinov.internal_data.IndexesProviderImpl
import ru.barinov.internal_data.InternalStorageProvider
import ru.barinov.internal_data.InternalStorageProviderImpl

val internalStorageProviderModule = module {

    factory {
        InternalStorageProviderImpl(get())
    } bind InternalStorageProvider::class

    factory {
        ContainerProviderImpl(androidContext())
    } bind  ContainerProvider::class

    factory {
        IndexesProviderImpl(androidContext())
    } bind IndexesProvider::class
}
