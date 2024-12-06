package ru.barinov.core.di

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.barinov.core.util.FileInfoExtractor
import ru.barinov.core.util.IndexTypeExtractor

val coreModule = module {
    factory {
        FileInfoExtractor(androidContext())
    } bind IndexTypeExtractor::class
}