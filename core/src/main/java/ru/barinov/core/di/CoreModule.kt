package ru.barinov.core.di

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.binds
import org.koin.dsl.module
import ru.barinov.core.util.EncryptedFileInfoExtractor
import ru.barinov.core.util.FileInfoExtractor
import ru.barinov.core.util.PlainFileInfoExtractor
import ru.barinov.core.util.IndexTypeExtractor

val coreModule = module {
    factory(Qualifiers.plaintFileInfoExtractor) {
        PlainFileInfoExtractor(androidContext())
    } binds arrayOf(IndexTypeExtractor::class, FileInfoExtractor::class)

    factory(Qualifiers.encryptedFileInfoExtractor) {
        EncryptedFileInfoExtractor()
    } bind FileInfoExtractor::class
}
