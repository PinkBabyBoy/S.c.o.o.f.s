package ru.barinov.file_browser.utils

interface FileSingleShareBus<T> {

    suspend fun get(): T?

    suspend fun share(data: T)
}
