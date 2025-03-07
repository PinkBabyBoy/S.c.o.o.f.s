package ru.barinov.file_browser

import ru.barinov.core.FileId

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

enum class BrowserRout{
    ENCRYPTION_START_BOTTOM_SHEET,
    CREATE_KEYSTORE_BOTTOM_SHEET,
    CREATE_CONTAINER_BOTTOM_SHEET,
    LOAD_KEYSTORE_BOTTOM_SHEET
}
