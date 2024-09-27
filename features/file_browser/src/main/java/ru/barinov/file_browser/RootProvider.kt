package ru.barinov.file_browser

import ru.barinov.core.FileEntity
import ru.barinov.core.Openable
import ru.barinov.core.Source

fun interface RootProvider {

    fun getRootFile(type: Source): Openable?
}
