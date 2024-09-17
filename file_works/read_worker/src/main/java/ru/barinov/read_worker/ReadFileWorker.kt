package ru.barinov.read_worker

import ru.barinov.core.FileEntity
import ru.barinov.core.FileIndex
import java.io.InputStream

interface ReadFileWorker {

    fun readIndexes(file: FileEntity.InternalFile): Result<List<FileIndex>>

    fun readKeyStore(fileEntity: FileEntity): InputStream
}