package ru.barinov.core

class FileIndex(
    val indexStartPoint: Long,// as is
    val startPoint: Long,// as is
    val sizeInIndexes: Int,
    val fileSize: Long,// as is
    val fileName: String,//read shortFirst to know the len
    val indexCreationTimeStamp: Long,
    val fileChangeTimeStamp: Long,
    val fileType: FileType,//byte
    val state: State // byte
) {
    enum class FileType {
        COMMON, PHOTO
    }

    enum class State {
        IDLE, EXTRACTED
    }
}

