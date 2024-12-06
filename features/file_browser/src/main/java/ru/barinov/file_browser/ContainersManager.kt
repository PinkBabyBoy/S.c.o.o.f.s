package ru.barinov.file_browser

import kotlinx.coroutines.flow.StateFlow
import ru.barinov.core.FileEntity
import java.io.File

interface ContainersManager {

    val indexes: StateFlow<List<FileEntity.Index>>

    suspend fun addContainer(name: String,  keysHash: ByteArray)

    fun removeContainer(name: String)

    fun getContainer(name: String): File
}
