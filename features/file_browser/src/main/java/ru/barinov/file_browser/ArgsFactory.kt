package ru.barinov.file_browser

import ru.barinov.core.FileId
import ru.barinov.core.Source

fun toImageDetails(fileId: FileId): ImageDetails {
    return ImageDetails(fileId.value)
}

fun toContainerContent(fileId: FileId): ContainersContent {
    return ContainersContent(fileId.value)
}

@kotlinx.serialization.Serializable
class ContainersContent(val fileId: String)

@kotlinx.serialization.Serializable
class ImageDetails(val fileId: String)
