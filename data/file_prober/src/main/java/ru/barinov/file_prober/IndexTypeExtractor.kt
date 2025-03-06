package ru.barinov.file_prober

import ru.barinov.core.FileEntity
import ru.barinov.core.FileIndex

fun interface IndexTypeExtractor {
    suspend fun getTypeDirectly(fileEntity: FileEntity): FileIndex.FileType
}

