package ru.barinov.file_browser

import kotlinx.coroutines.flow.StateFlow
import ru.barinov.core.FileEntity

interface ContainersManager {

    val indexes: StateFlow<List<FileEntity.Index>>

    fun addContainer(name: String)

    fun removeContainer(name: String)
}
