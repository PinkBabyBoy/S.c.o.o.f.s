package ru.barinov.file_works

import ru.barinov.core.FileEntity
import ru.barinov.core.FileIndex
import ru.barinov.core.getBytes
import java.lang.IllegalArgumentException
import java.nio.ByteBuffer

object IndexCreator {

    fun createIndex(
        fileEntity: FileEntity,
        currentIndexPos: Long,
        currentContainerPos: Long,
        fileType: FileIndex.FileType
    ): ByteArray {
        if (fileEntity.isDir) throw IllegalArgumentException()
        return fileEntity.name.value.encodeToByteArray() +
                currentContainerPos.getBytes() +
                fileEntity.size.value.getBytes() +
                fileEntity.lastModifiedTimeStamp.getBytes() +
                System.currentTimeMillis().getBytes() +
                fileType.ordinal.getBytes() +
                FileIndex.State.IDLE.ordinal.getBytes()

    }

    fun restoreIndex(decryptedIndex: ByteArray, startPos: Long, rawSize: Int): FileIndex =
        ByteBuffer.wrap(decryptedIndex).run {
            val nameSize = getShort()
            FileIndex(
                fileName = ByteArray(nameSize.toInt()).also { get(it) }.decodeToString(),
                startPoint = getLong(),
                fileSize = getLong(),
                fileChangeTimeStamp = getLong(),
                indexCreationTimeStamp = getLong(),
                fileType = FileIndex.FileType.entries[get().toInt()],
                state = FileIndex.State.entries[get().toInt()],
                sizeInIndexes = rawSize,
                indexStartPoint = startPos,
            )
        }
}
