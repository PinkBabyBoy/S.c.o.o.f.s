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
            val hash = ByteArray(ByteBuffer.wrap(sizeBuffer).getInt())
            it.read(hash)
            snapshotKeyStorage.decrypt(hash)
        }
}

fun interface ContainerHashExtractor{

    fun extractHash(index: FileEntity.Index): ByteArray
}
