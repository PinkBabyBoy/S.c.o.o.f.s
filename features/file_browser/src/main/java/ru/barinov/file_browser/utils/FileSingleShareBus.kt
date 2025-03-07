package ru.barinov.file_browser.utils


abstract class FileSingleShareBus<T> {

    protected val holder = mutableMapOf<Key, T>()

    abstract suspend fun get(key: Key, keep: Boolean = false): T?

    abstract suspend fun share(key: Key, data: T)

    enum class Key{
        IMAGE_SHARE, ENCRYPTION
    }

    abstract suspend fun clear()

}



