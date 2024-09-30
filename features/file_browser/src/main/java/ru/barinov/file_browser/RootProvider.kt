package ru.barinov.file_browser

import ru.barinov.core.Addable
import ru.barinov.core.Source

fun interface RootProvider {

    fun getRootFile(type: Source): Addable?
}
