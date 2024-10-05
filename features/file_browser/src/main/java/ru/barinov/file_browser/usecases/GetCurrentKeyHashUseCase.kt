package ru.barinov.file_browser.usecases

import ru.barinov.cryptography.KeyMemoryCache
import ru.barinov.cryptography.hash.HashCreator
import ru.barinov.cryptography.hash.HashMode

class GetCurrentKeyHashUseCase(
    private val keyMemoryCache: KeyMemoryCache,
    private val hashCreator: HashCreator
) {

    operator fun invoke() = hashCreator.createHash(keyMemoryCache.getPublicKey()!!.encoded, HashMode.KEY_HASH)
}