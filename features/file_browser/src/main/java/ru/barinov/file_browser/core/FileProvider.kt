package ru.barinov.file_browser.core

import ru.barinov.core.Addable
import ru.barinov.core.FileId
import ru.barinov.core.Source

fun interface FileProvider {

    fun getFileByID(fileId: FileId, source: Source): Addable
}

