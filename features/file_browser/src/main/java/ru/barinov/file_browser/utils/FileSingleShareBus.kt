package ru.barinov.file_browser.utils

import kotlinx.coroutines.sync.withLock
import ru.barinov.core.InteractableFile

interface FileSingleShareBus {

    suspend fun get(): InteractableFile?

    suspend fun share(file: InteractableFile)
}
