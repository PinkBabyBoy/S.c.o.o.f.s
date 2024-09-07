package ru.barinov.external_data.di

import org.koin.dsl.module
import ru.barinov.external_data.GetMSDFileSystemUseCase

val msdProviderModule =  module {

    factory {
        GetMSDFileSystemUseCase(get())
    }
}
