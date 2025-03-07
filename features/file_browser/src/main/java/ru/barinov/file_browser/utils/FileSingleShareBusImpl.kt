package ru.barinov.file_browser.utils

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.barinov.core.InteractableFile

internal class FileSingleShareBusImpl : FileSingleShareBus<InteractableFile>() {

    private val mutex = Mutex()


    override suspend fun get(key: Key, keep: Boolean) = mutex.withLock {
        if(keep) holder[key] else holder.remove(key)
    }

    override suspend fun clear() {
        mutex.withLock {
            holder.clear()
        }
    }


    override suspend fun share(key: Key, data: InteractableFile) =
        mutex.withLock { holder[key] = data }
}
