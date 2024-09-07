package ru.barinov.filework

import ru.barinov.core.FileEntity
import ru.barinov.core.FileIndex

interface ReadFileWorker {

    fun readIndexes(file: FileEntity.InternalFile): Result<List<FileIndex>>

    fun readRawKey(fileEntity: FileEntity): ByteArray
}