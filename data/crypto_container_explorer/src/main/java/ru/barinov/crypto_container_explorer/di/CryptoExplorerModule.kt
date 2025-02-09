package ru.barinov.crypto_container_explorer.di

import org.koin.dsl.bind
import org.koin.dsl.module
import ru.barinov.crypto_container_explorer.CryptoRepository
import ru.barinov.crypto_container_explorer.CryptoRepositoryImpl

val cryptoExplorerModule = module {
    single {
        CryptoRepositoryImpl(get(), get())
    } bind  CryptoRepository::class
}
