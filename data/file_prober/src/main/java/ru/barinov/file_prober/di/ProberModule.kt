package ru.barinov.file_prober.di

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.binds
import org.koin.dsl.module
import ru.barinov.file_prober.EncryptedFileInfoExtractor
import ru.barinov.file_prober.PlainFileInfoExtractor

val proberModule = module {
    factory(Qualifiers.plaintFileInfoExtractor) {
       PlainFileInfoExtractor(androidContext())
    } binds arrayOf(ru.barinov.file_prober.IndexTypeExtractor::class, ru.barinov.file_prober.FileInfoExtractor::class)

    factory(Qualifiers.encryptedFileInfoExtractor) {
        EncryptedFileInfoExtractor(get())
    } bind ru.barinov.file_prober.FileInfoExtractor::class
}
