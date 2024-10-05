package ru.barinov.password_manager.di

import org.koin.dsl.bind
import org.koin.dsl.module
import ru.barinov.password_manager.PasswordStorage
import ru.barinov.password_manager.PasswordStorageImpl

val passwordManagerModule = module {

    factory {
        PasswordStorageImpl(
            secretKeyGen = get(),
            cipherFactory = get(),
            storage = get()
        )
    } bind PasswordStorage::class
}
