package ru.barinov.core.util

import kotlinx.coroutines.flow.StateFlow
import ru.barinov.core.FileTypeInfo
import ru.barinov.core.StorageAble

interface  FileInfoExtractor <T: StorageAble> {

    fun clear()

    operator fun  invoke(
        fileEntity: T,
        recognizeOn: Boolean
    ): StateFlow<FileTypeInfo>

}