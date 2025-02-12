package ru.barinov.core.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.barinov.core.InteractableFile
import ru.barinov.core.FileEntity
import ru.barinov.core.FileId
import ru.barinov.core.FileIndex
import ru.barinov.core.FileTypeInfo
import ru.barinov.core.StorageAble
import ru.barinov.core.bytesToMbSting
import ru.barinov.core.inputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val BIG_FILE_SIZE_LIMIT = 1024 * 1024 * 20
private const val THREAD_LIMIT = 8

internal class PlainFileInfoExtractor(
    private val appContext: Context
): IndexTypeExtractor, FileInfoExtractor<FileEntity> {

    @OptIn(ExperimentalCoroutinesApi::class)
    private val recognizerCoroutineScope = CoroutineScope(Job() + Dispatchers.IO.limitedParallelism(
        THREAD_LIMIT
    ))
    private val savedInfos = mutableMapOf<FileId, StateFlow<FileTypeInfo>>()

    override suspend fun getTypeDirectly(fileEntity: FileEntity): FileIndex.FileType{
        //TODO return real type
      return  FileIndex.FileType.COMMON
    }

    override fun clear() {
        recognizerCoroutineScope.coroutineContext.cancelChildren()
        recognizerCoroutineScope.launch {
            savedInfos.forEach {
                (it.value.value as? FileTypeInfo.ImageFile)?.bitmapPreview?.recycle()
            }
            savedInfos.clear()
        }
    }

    override operator fun invoke(
        fileEntity: FileEntity,
        recognizeOn: Boolean
    ): StateFlow<FileTypeInfo> {
        if (savedInfos.containsKey(fileEntity.fileId)) {
            return savedInfos[fileEntity.fileId]!!
        }
        val state: MutableStateFlow<FileTypeInfo> = MutableStateFlow(FileTypeInfo.Unconfirmed)
        if (recognizeOn) {
            recognizerCoroutineScope.launch {
                when {

                    fileEntity is FileEntity.IndexStorage -> {
                        FileTypeInfo.IndexStorage(
                            SimpleDateFormat(
                                "dd-MM-yyyy",
                                Locale.getDefault()
                            ).format(Date(fileEntity.attachedOrigin.lastModified()))
                        )
                    }

                    fileEntity.isDir -> {
                        val contentCount = fileEntity.containsCount() ?: 0
                        FileTypeInfo.Dir(
                            appContext.getString(
                                ru.barinov.core.R.string.contains_files_info,
                                contentCount
                            ),
                            contentCount
                        )
                    }

                    fileEntity.size.value > BIG_FILE_SIZE_LIMIT -> FileTypeInfo.Other(
                        true,
                        fileEntity.size.value.bytesToMbSting()
                    )

                    (fileEntity as? InteractableFile)?.inputStream()?.isImage() == true -> {
                        val iStream = (fileEntity as? InteractableFile)?.inputStream() ?: return@launch
                        iStream.use {
                            val preview = it.getBitMapPreview()
                            if (preview == null) {
                                FileTypeInfo.Other(false, fileEntity.size.value.bytesToMbSting())
                            } else {
                                FileTypeInfo.ImageFile(
                                    bitmapPreview = preview,
                                    size = fileEntity.size.value.bytesToMbSting()
                                )
                            }
                        }

                    }

                    else ->  FileTypeInfo.Other(false, fileEntity.size.value.bytesToMbSting())
                }.also {
                    state.value = it
                    savedInfos[fileEntity.fileId] = state
                }
            }
        }
        return state.asStateFlow()
    }

    private suspend fun InputStream.getBitMapPreview(): Bitmap? = runCatching {
        BitmapFactory.decodeStream(this)?.let { Bitmap.createScaledBitmap(it, 100, 100, true) }
    }.getOrNull()

    private suspend fun InputStream.isImage(): Boolean = runCatching {
        val bitmapOptions = BitmapFactory.Options().also { it.inJustDecodeBounds = true }
        BitmapFactory.decodeStream(this, null, bitmapOptions)
        return (bitmapOptions.outWidth != -1 && bitmapOptions.outHeight != -1).also { close() }
    }.getOrNull() ?: false
}
