package ru.barinov.plain_explorer.di

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.barinov.plain_explorer.FileProvider
import ru.barinov.plain_explorer.FileTreeProvider
import ru.barinov.plain_explorer.repository.PlainDataRepository
import ru.barinov.plain_explorer.repository.PlainRepositoryImpl

val plainExplorerModule = module {

    factory {
        FileTreeProvider(get()) { androidContext().resources.getString(-1) }
    } bind FileProvider::class

    single {
        PlainRepositoryImpl(get())
    } bind  PlainDataRepository::class
}
