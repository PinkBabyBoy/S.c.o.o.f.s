package ru.barinov.file_browser.utils

import androidx.lifecycle.ViewModel
import ru.barinov.core.InteractableFile

abstract class FileSingleShareBus<T> {

    protected val holder = mutableMapOf<Key, T>()

    abstract suspend fun get(key: Key): T?

    abstract suspend fun share(key: Key, data: T)

    enum class Key{
        IMAGE_SHARE, ENCRYPTION
    }

}



