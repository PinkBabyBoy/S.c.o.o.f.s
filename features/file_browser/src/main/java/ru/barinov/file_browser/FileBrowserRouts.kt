package ru.barinov.file_browser

import ru.barinov.core.FileId
import ru.barinov.core.Filename
import ru.barinov.core.Source

fun toImageDetails(fileId: FileId): ImageDetails {
    return ImageDetails(fileId.value)
}

fun toContainerContent(fileId: FileId): ContainersContent {
    return ContainersContent(fileId.value)
}

fun loadKeyStore(source: Source, filename: Filename) = LoadKeyStore(source, filename.value)

@kotlinx.serialization.Serializable
class ContainersContent(val fileId: String)

@kotlinx.serialization.Serializable
class ImageDetails(val fileId: String)

@kotlinx.serialization.Serializable
class LoadKeyStore(val source: Source, val filename: String)

enum class NoArgsRouts{
    ENCRYPTION_START_BOTTOM_SHEET,
    CREATE_KEYSTORE_BOTTOM_SHEET,
    CREATE_CONTAINER_BOTTOM_SHEET,
}
