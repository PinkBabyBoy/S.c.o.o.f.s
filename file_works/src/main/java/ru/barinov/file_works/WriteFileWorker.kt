package ru.barinov.filework

import kotlinx.coroutines.flow.MutableSharedFlow
import ru.barinov.core.FileEntity
import java.io.File

interface WriteFileWorker {

    suspend fun putInStorage(
        targetFile: FileEntity,
        progressFlow: MutableSharedFlow<Long>?,
        indexes: File,
        container: File
    )

    suspend fun deleteEntries(innerFolder: File, names: List<String>)
}
