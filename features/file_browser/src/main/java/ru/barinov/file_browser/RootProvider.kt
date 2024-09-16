package ru.barinov.file_browser

import ru.barinov.core.FileEntity
import ru.barinov.core.Source

interface RootProvider {

    fun getRootFile(type: Source): FileEntity?
}
