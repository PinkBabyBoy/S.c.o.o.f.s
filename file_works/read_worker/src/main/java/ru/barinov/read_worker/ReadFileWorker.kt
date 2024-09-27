package ru.barinov.read_worker

import ru.barinov.core.FileEntity
import ru.barinov.core.FileIndex
import java.io.File
import java.io.InputStream

interface ReadFileWorker {

    suspend fun readIndexes(file: File): Result<List<FileIndex>>

}
