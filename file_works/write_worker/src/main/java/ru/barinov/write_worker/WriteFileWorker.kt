package ru.barinov.write_worker

import ru.barinov.core.FileEntity
import java.io.File

interface WriteFileWorker {

    suspend fun putInStorage(
        targetFile: FileEntity,
        progressCallback: (Long) -> Unit,
        indexes: File,
        container: File
    )

    suspend fun deleteEntries(innerFolder: File, names: List<String>)
}
