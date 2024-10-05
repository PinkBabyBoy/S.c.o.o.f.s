package ru.barinov.file_browser.usecases

import ru.barinov.cryptography.KeyMemoryCache
import ru.barinov.cryptography.hash.utils.KeySnapshotCreator

class GetSerializableCurrentKeyHashUseCase(
    private val keyCache: KeyMemoryCache,
    private val keySnapshotCreator: KeySnapshotCreator,
) {

    operator fun invoke(): ByteArray =
        keySnapshotCreator.createFrom(keyCache.getPublicKey()!!)
}
