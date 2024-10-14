package ru.barinov.file_browser.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.barinov.core.Addable
import ru.barinov.core.FileEntity
import ru.barinov.core.FileId
import ru.barinov.core.bytesToMbSting
import ru.barinov.core.inputStream
import ru.barinov.core.mb
import ru.barinov.file_browser.models.FileInfo
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FileInfoExtractor(
    private val appContext: Context
) {

    private val recognizerCoroutineScope = CoroutineScope(Job() + Dispatchers.IO)
    private val savedInfos = mutableMapOf<FileId, MutableState<FileInfo>>()

    fun clear() {
        recognizerCoroutineScope.coroutineContext.cancelChildren()
        recognizerCoroutineScope.launch {
            savedInfos.forEach {
                (it.value.value as? FileInfo.ImageFile)?.bitmapPreview?.recycle()
            }
            savedInfos.clear()
        }
    }

    operator fun invoke(
        fileEntity: FileEntity,
        recognizeOn: Boolean,
        delayDuration: Long = 0L
    ): MutableState<FileInfo> {
        if (savedInfos.containsKey(fileEntity.fileId)) {
            return savedInfos[fileEntity.fileId]!!
        }
        val state: MutableState<FileInfo> = mutableStateOf(FileInfo.Unconfirmed)
        if (recognizeOn) {
            recognizerCoroutineScope.launch {

                if (delayDuration > 0) delay(delayDuration)

                when {

                    fileEntity is FileEntity.Index -> {
                        FileInfo.Index(
                            SimpleDateFormat(
                                "dd-MM-yyyy",
                                Locale.getDefault()
                            ).format(Date(fileEntity.attachedOrigin.lastModified()))
                        )
                    }

                    fileEntity.isDir -> {
                        val contentCount = fileEntity.containsCount() ?: 0
                        FileInfo.Dir(
                            appContext.getString(
                                ru.barinov.core.R.string.contains_files_info,
                                contentCount
                            ),
                            contentCount
                        )
                    }

                    fileEntity.size.value.mb() > 7 -> FileInfo.Other(
                        true,
                        fileEntity.size.value.bytesToMbSting()
                    )

                    (fileEntity as? Addable)?.inputStream()?.isImage() == true -> {
                        val iStream = (fileEntity as? Addable)?.inputStream() ?: return@launch
                        iStream.use {
                            val preview = it.getBitMapPreview()
                            if (preview == null) {
                                FileInfo.Other(false, fileEntity.size.value.bytesToMbSting())
                            } else {
                                FileInfo.ImageFile(
                                    bitmapPreview = preview,
                                    size = fileEntity.size.value.bytesToMbSting()
                                )
                            }
                        }

                    }

                    else ->  FileInfo.Other(false, fileEntity.size.value.bytesToMbSting())
                }.also {
                    state.value = it
                    savedInfos[fileEntity.fileId] = state
                }
            }
        }
        return state
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
