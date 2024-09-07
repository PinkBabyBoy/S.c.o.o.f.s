package ru.barinov.password_manager.di

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.barinov.password_manager.HashCreator
import ru.barinov.password_manager.HashCreatorImpl
import ru.barinov.password_manager.HashValidator
import ru.barinov.password_manager.HashValidatorImpl
import ru.barinov.password_manager.PasswordStorage
import ru.barinov.password_manager.PasswordStorageImpl

val passwordManagerModule = module {

    factory {
        HashCreatorImpl()
    } bind  HashCreator::class

    factory {
        HashValidatorImpl()
    } bind HashValidator::class

    single {
        PasswordStorageImpl(
            secretKeyGen = get(),
            cipherFactory = get(),
            storage = get()
        )
    } bind PasswordStorage::class
}
