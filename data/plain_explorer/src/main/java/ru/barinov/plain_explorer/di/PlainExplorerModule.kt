package ru.barinov.plain_explorer.di

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.barinov.plain_explorer.FileProvider
import ru.barinov.plain_explorer.FolderTreeAgentImpl
import ru.barinov.plain_explorer.interactor.FolderDataInteractor
import ru.barinov.plain_explorer.interactor.FolderDataInteractorImpl

val plainExplorerModule = module {

    factory {
        FolderTreeAgentImpl(get()) { androidContext().resources.getString(-1) }
    } bind FileProvider::class

    single {
        FolderDataInteractorImpl(get())
    } bind  FolderDataInteractor::class
}
