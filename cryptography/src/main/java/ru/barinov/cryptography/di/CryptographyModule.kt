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
import ru.barinov.cryptography.SnapshotKeyStorage
import ru.barinov.cryptography.SnapshotKeyStorageImpl
import ru.barinov.cryptography.factories.CipherFactoryImpl
import ru.barinov.cryptography.factories.CipherFactory
import ru.barinov.cryptography.factories.CipherStreamsFactory
import ru.barinov.cryptography.factories.CipherStreamsFactoryImpl
import ru.barinov.cryptography.factories.KeyStoreFactory
import ru.barinov.cryptography.factories.KeyStoreFactoryImpl
import ru.barinov.cryptography.hash.HashCreator
import ru.barinov.cryptography.hash.HashCreatorImpl
import ru.barinov.cryptography.hash.HashValidator
import ru.barinov.cryptography.hash.HashValidatorImpl
import ru.barinov.cryptography.keygens.AsymmetricKeyGenerator
import ru.barinov.cryptography.keygens.AsymmetricKeyGeneratorImpl
import ru.barinov.cryptography.keygens.SecretKeyGenerator
import ru.barinov.cryptography.keygens.SecretKeyGeneratorImpl
import ru.barinov.cryptography.hash.utils.ContainerHashExtractor
import ru.barinov.cryptography.hash.utils.ContainerHashExtractorImpl
import ru.barinov.cryptography.hash.utils.KeySnapshotCreator
import ru.barinov.cryptography.hash.utils.KeySnapshotCreatorImpl

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
        DecryptorImpl(get())
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
        )
    } bind KeyManager::class

    single {
        KeyStoreFactoryImpl(get())
    } bind KeyStoreFactory::class

    factory {
        SnapshotKeyStorageImpl(secretKeyGen = get(), cipherFactory = get())
    } bind SnapshotKeyStorage::class

    factory {
        KeySnapshotCreatorImpl(
            snapshotKeyStorage = get(),
            hashCreator = get()
        )
    } bind KeySnapshotCreator::class

    factory {
        ContainerHashExtractorImpl(get())
    } bind ContainerHashExtractor::class

    factory {
        HashCreatorImpl()
    } bind  HashCreator::class

    factory {
        HashValidatorImpl()
    } bind HashValidator::class
}
