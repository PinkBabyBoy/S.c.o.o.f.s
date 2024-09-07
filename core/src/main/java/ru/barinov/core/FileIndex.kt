package ru.barinov.core

class FileIndex(
    val indexStartPoint: Long,// as is
    val startPoint: Long,// as is
    val fileSize: Long,// as is
    val fileName: String,//read shortFirst to know the len
    val creationTimeStamp: Long,
    val fileType: FileType//byte
)

enum class FileType