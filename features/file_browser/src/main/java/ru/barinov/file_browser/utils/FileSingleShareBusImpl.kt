package ru.barinov.file_browser.utils

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.barinov.core.InteractableFile

internal class FileSingleShareBusImpl : FileSingleShareBus<InteractableFile>() {

    private val mutex = Mutex()


    override suspend fun get(key: Key) = mutex.withLock {
        holder.remove(key)
    }


    override suspend fun share(key: Key, data: InteractableFile) =
        mutex.withLock { holder[key] = data }
}
