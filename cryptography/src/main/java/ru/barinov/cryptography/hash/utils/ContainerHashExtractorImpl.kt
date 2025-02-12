package ru.barinov.cryptography.hash.utils

import ru.barinov.core.FileEntity
import ru.barinov.cryptography.SnapshotKeyStorage
import java.nio.ByteBuffer

internal class ContainerHashExtractorImpl(
    private val snapshotKeyStorage: SnapshotKeyStorage
): ContainerHashExtractor {

    override fun extractHash(index: FileEntity.IndexStorage): ByteArray =
        index.attachedOrigin.inputStream().use {
            val totalSize = ByteArray(Int.SIZE_BYTES).run {
                it.read(this)
                ByteBuffer.wrap(this)
            }.getInt()
            val ivSize = ByteArray(Int.SIZE_BYTES).run {
                it.read(this)
                ByteBuffer.wrap(this)
            }.getInt()
            val iv = ByteArray(ivSize).apply {
                it.read(this)
            }
            val encHashSize =ByteArray(Int.SIZE_BYTES).run {
                it.read(this)
                ByteBuffer.wrap(this)
            }.getInt()
            val encHash = ByteArray(encHashSize).apply {
                it.read(this)
            }
            snapshotKeyStorage.decrypt(encHash, iv)
        }
}

fun interface ContainerHashExtractor{

    fun extractHash(index: FileEntity.IndexStorage): ByteArray
}
