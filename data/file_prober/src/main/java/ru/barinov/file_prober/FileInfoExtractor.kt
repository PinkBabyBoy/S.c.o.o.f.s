package ru.barinov.file_prober

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import kotlinx.coroutines.flow.StateFlow
import ru.barinov.core.FileTypeInfo
import ru.barinov.core.StorageAble
import java.io.InputStream

const val BIG_FILE_SIZE_LIMIT = 1024 * 1024 * 20
const val THREAD_LIMIT = 8

abstract class FileInfoExtractor <T: StorageAble> {

    abstract fun clear()

    abstract operator fun  invoke(
        fileEntity: T,
        recognizeOn: Boolean
    ): StateFlow<FileTypeInfo>

    protected suspend fun InputStream.getBitMapPreview(): Bitmap? = runCatching {
        BitmapFactory.decodeStream(this)?.let { Bitmap.createScaledBitmap(it, 100, 100, true) }
    }.onSuccess { Log.e("@@@", "SUCC BM $it") }.onFailure { Log.e("@@@", it.stackTraceToString()) }.getOrNull()

    protected suspend fun InputStream.isImage(): Boolean = runCatching {
        val bitmapOptions = BitmapFactory.Options().also { it.inJustDecodeBounds = true }
        BitmapFactory.decodeStream(this, null, bitmapOptions)
        return@runCatching (bitmapOptions.outWidth != -1 && bitmapOptions.outHeight != -1).also { close() }
    }.onSuccess {  Log.e("@@@", "IS BM $it") }.onFailure { Log.e("@@@", it.stackTraceToString()) }.getOrNull() ?: false

}
