package ru.barinov.read_worker

import ru.barinov.core.FileIndex
import java.io.File

interface ReadFileWorker {

    @Deprecated("Use paging implementation")
    suspend fun readIndexes(container: File, limit: Int): List<FileIndex>

    suspend fun getIndexesByOffsetAndLimit(container: File, from: Long, limit: Int): List<FileIndex>

}
