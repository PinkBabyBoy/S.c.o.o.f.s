package ru.barinov.core.util

import kotlinx.coroutines.flow.StateFlow
import ru.barinov.core.FileIndex
import ru.barinov.core.FileTypeInfo
import ru.barinov.core.StorageAble

//TODO Implement
internal class EncryptedFileInfoExtractor: FileInfoExtractor<FileIndex> {

    override fun clear() {
        TODO("Not yet implemented")
    }

     override fun invoke(fileEntity: FileIndex, recognizeOn: Boolean): StateFlow<FileTypeInfo> {
         TODO("Not yet implemented")
     }

}
