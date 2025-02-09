package ru.barinov.plain_explorer

import ru.barinov.core.InteractableFile
import ru.barinov.core.FileId
import ru.barinov.core.Source

fun interface FileProvider {

    fun getFileByID(fileId: FileId, source: Source): InteractableFile
}

