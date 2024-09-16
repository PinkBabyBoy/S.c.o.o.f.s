package ru.barinov.cryptography.di

import org.koin.dsl.bind
import org.koin.dsl.module
import ru.barinov.cryptography.Decryptor
import ru.barinov.cryptography.DecryptorImpl
import ru.barinov.cryptography.Encryptor
import ru.barinov.cryptography.EncryptorImpl
import ru.barinov.cryptography.KeyManager
import ru.barinov.cryptography.KeyManagerImpl
import ru.barinov.cryptography.KeyMemoryCache
import ru.barinov.cryptography.KeyMemoryCacheImpl
import ru.barinov.cryptography.factories.CipherFactoryImpl
import ru.barinov.cryptography.factories.CipherFactory
import ru.barinov.cryptography.factories.CipherStreamsFactory
import ru.barinov.cryptography.factories.CipherStreamsFactoryImpl
import ru.barinov.cryptography.keygens.AsymmetricKeyGenerator
import ru.barinov.cryptography.keygens.AsymmetricKeyGeneratorImpl
import ru.barinov.cryptography.keygens.SecretKeyGenerator
import ru.barinov.cryptography.keygens.SecretKeyGeneratorImpl

val cryptographyModule = module{

    factory {
        CipherFactoryImpl(get())
    } bind CipherFactory::class

    factory {
        SecretKeyGeneratorImpl()
    } bind SecretKeyGenerator::class

    factory {
        AsymmetricKeyGeneratorImpl()
    } bind AsymmetricKeyGenerator::class

    single {
        KeyMemoryCacheImpl()
    } bind KeyMemoryCache::class

    factory {
        DecryptorImpl()
    } bind Decryptor::class

    factory {
        EncryptorImpl(get(), get())
    } bind Encryptor::class

    factory {
        CipherStreamsFactoryImpl(get())
    } bind CipherStreamsFactory::class


    single {
        KeyManagerImpl(
            keyCache = get(),
            readFileWorker = get(),
        )
    } bind KeyManager::class
}
