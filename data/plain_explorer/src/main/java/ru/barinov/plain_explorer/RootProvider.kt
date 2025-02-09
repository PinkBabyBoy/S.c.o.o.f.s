package ru.barinov.plain_explorer

import ru.barinov.core.InteractableFile
import ru.barinov.core.Source

fun interface RootProvider {

    fun getRootFile(type: Source): InteractableFile?
}
