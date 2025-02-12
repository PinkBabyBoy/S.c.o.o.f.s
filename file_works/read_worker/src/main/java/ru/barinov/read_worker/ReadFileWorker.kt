package ru.barinov.read_worker

import ru.barinov.core.FileIndex
import java.io.File

interface ReadFileWorker {

    @Deprecated("Use paging implementation")
    suspend fun readIndexes(file: File, limit: Int): List<FileIndex>

    suspend fun getIndexesByOffsetAndLimit(file: File, from: Long, limit: Int): List<FileIndex>

}
