package ru.barinov.file_browser.utils

import ru.barinov.core.FileEntity
import ru.barinov.core.FileIndex

fun interface IndexTypeExtractor {
    suspend fun getTypeDirectly(fileEntity: FileEntity): FileIndex.FileType
}