package ru.barinov.file_browser

import kotlinx.coroutines.flow.StateFlow
import ru.barinov.core.FileEntity

interface ContainersManager {

    val indexes: StateFlow<List<FileEntity.Index>>

    suspend fun addContainer(name: String,  keysHash: ByteArray)

    fun removeContainer(name: String)
}
