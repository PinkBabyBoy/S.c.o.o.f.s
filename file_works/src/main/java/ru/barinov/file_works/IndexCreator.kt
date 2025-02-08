package ru.barinov.file_works

import android.util.Log
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
        //S/Sn/L/L/L/L/I/I
        if (fileEntity.isDir) throw IllegalArgumentException()
        return fileEntity.name.value.length.toShort().getBytes() + //
                fileEntity.name.value.encodeToByteArray() + //
                currentContainerPos.getBytes() + //
                fileEntity.size.value.getBytes() + //
                fileEntity.lastModifiedTimeStamp.getBytes() + //
                System.currentTimeMillis().getBytes() + //
                fileType.ordinal.getBytes() + //
                FileIndex.State.IDLE.ordinal.getBytes() //

    }

    fun restoreIndex(decryptedIndex: ByteArray, startPos: Long, rawSize: Int): FileIndex =
        ByteBuffer.wrap(decryptedIndex).run {
            //S/Sn/L/L/L/L/I/I
            FileIndex(
                fileName = ByteArray(getShort().toInt()).apply(::get).decodeToString(),
                startPoint = getLong(),
                fileSize = getLong(),
                fileChangeTimeStamp = getLong(),
                indexCreationTimeStamp = getLong(),
                fileType = FileIndex.FileType.entries[getInt()],
                state = FileIndex.State.entries[getInt()],
                sizeInIndexes = rawSize,
                indexStartPoint = startPos,
            )
        }
}
