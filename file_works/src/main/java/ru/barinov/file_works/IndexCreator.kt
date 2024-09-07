package ru.barinov.file_works

import ru.barinov.core.FileEntity
import ru.barinov.filework.getBytes
import java.lang.IllegalArgumentException

internal object IndexCreator {

//    const val TAG_SIZE = 128

    fun createIndex(
        fileEntity: FileEntity,
        currentIndexPos: Long,
        currentContainerPos: Long
    ): ByteArray {
        if (fileEntity.isDir) throw IllegalArgumentException()
        return currentIndexPos.getBytes() +
                currentContainerPos.getBytes() +
                fileEntity.size.value.getBytes() +
                fileEntity.name.value.toByteArray() +
                System.currentTimeMillis().getBytes()
        //TODO FILE TYPE

    }

    fun restoreIndex(tagBuffer: ByteArray, startPos: Long) {

    }
}