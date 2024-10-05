package ru.barinov.cryptography.hash.utils

import ru.barinov.core.FileEntity
import ru.barinov.cryptography.SnapshotKeyStorage
import java.nio.ByteBuffer

internal class ContainerHashExtractorImpl(
    private val snapshotKeyStorage: SnapshotKeyStorage
): ContainerHashExtractor {

    override fun extractHash(index: FileEntity.Index): ByteArray =
        index.attachedOrigin.inputStream().use {
            val sizeBuffer = ByteArray(Int.SIZE_BYTES)
            it.read(sizeBuffer)
            val payload = ByteArray(ByteBuffer.wrap(sizeBuffer).getInt())
            it.read(payload)
            val hashBuffer = ByteBuffer.wrap(payload)
            val ivSize = hashBuffer.getInt()
            val iv = ByteArray(ivSize).also { hashBuffer.get(it) }
            val encHashSize = hashBuffer.getInt()
            val encHash = ByteArray(encHashSize).also { hashBuffer.get(it) }
            snapshotKeyStorage.decrypt(encHash, iv)
        }
}

fun interface ContainerHashExtractor{

    fun extractHash(index: FileEntity.Index): ByteArray
}
