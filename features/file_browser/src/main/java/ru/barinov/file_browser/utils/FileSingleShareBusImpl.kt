package ru.barinov.file_browser.utils

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.barinov.core.InteractableFile

internal class FileSingleShareBusImpl : FileSingleShareBus<InteractableFile> {

    private val mutex = Mutex()

    private var file: InteractableFile? = null

    override suspend fun get() = mutex.withLock {
        val temp = file
        file = null
        return@withLock temp
    }


    override suspend fun share(data: InteractableFile) = mutex.withLock { this.file = data }
}
