package ru.barinov.file_browser.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.barinov.core.FileIndex
import ru.barinov.core.FileTypeInfo
import ru.barinov.read_worker.ReadFileWorker

//TODO Implement
internal class EncryptedFileInfoExtractor(
    private val readFileWorker: ReadFileWorker
): FileInfoExtractor<FileIndex> {

    override fun clear() {
        TODO("Not yet implemented")
    }

     override fun invoke(fileEntity: FileIndex, recognizeOn: Boolean): StateFlow<FileTypeInfo> {
         return MutableStateFlow(FileTypeInfo.Unconfirmed)
     }

}