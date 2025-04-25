package ru.barinov.file_prober

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.barinov.core.FileId
import ru.barinov.core.FileIndex
import ru.barinov.core.FileTypeInfo
import ru.barinov.core.bytesToMbSting
import ru.barinov.read_worker.ReadFileWorker

//TODO Implement
internal class EncryptedFileInfoExtractor(
    private val readFileWorker: ReadFileWorker
) : FileInfoExtractor<FileIndex>() {

    @OptIn(ExperimentalCoroutinesApi::class)
    private val recognizerCoroutineScope = CoroutineScope(
        Job() + Dispatchers.IO.limitedParallelism(
            THREAD_LIMIT
        )
    )
    private val savedInfos = mutableMapOf<FileId, StateFlow<FileTypeInfo>>()

    override fun clear() {
        recognizerCoroutineScope.coroutineContext.cancelChildren()
        recognizerCoroutineScope.launch {
            savedInfos.forEach {
                (it.value.value as? FileTypeInfo.ImageFile)?.bitmapPreview?.recycle()
            }
            savedInfos.clear()
        }
    }

    override fun invoke(fileEntity: FileIndex, recognizeOn: Boolean): StateFlow<FileTypeInfo> {
        if (savedInfos.containsKey(fileEntity.id)) {
            return savedInfos[fileEntity.id]!!
        }
        val state: MutableStateFlow<FileTypeInfo> = MutableStateFlow(FileTypeInfo.Unconfirmed)
        if (recognizeOn) {
            recognizerCoroutineScope.launch {
                if (readFileWorker.readFile(fileEntity).isImage()) {
                    val iStream = readFileWorker.readFile(fileEntity)
                    iStream.use {
                        val preview = it.getBitMapPreview()
                        if (preview == null) {
                            state.emit(
                                FileTypeInfo.Other(
                                    false,
                                    fileEntity.fileSize.bytesToMbSting()
                                )
                            )
                        } else {
                            state.emit(
                                FileTypeInfo.ImageFile(
                                    bitmapPreview = preview,
                                    size = fileEntity.fileSize.bytesToMbSting()
                                )
                            )
                        }
                    }
                } else state.emit(FileTypeInfo.Other(false, fileEntity.fileSize.bytesToMbSting()))
            }
        }
        return state.asStateFlow()

    }

}
