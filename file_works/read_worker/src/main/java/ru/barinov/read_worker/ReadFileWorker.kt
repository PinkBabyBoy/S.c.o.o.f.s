package ru.barinov.read_worker

import ru.barinov.core.FileIndex
import java.io.File
import java.io.InputStream

interface ReadFileWorker {

    suspend fun readFile(index: FileIndex): InputStream

    @Deprecated("Use paging implementation")
    suspend fun readIndexes(container: File, limit: Int): List<FileIndex>

    suspend fun getIndexesByOffsetAndLimit(indexes: File, container: File, from: Long, limit: Int): List<FileIndex>

}
