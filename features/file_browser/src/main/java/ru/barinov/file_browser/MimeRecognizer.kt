package ru.barinov.file_browser

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.MutableState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import ru.barinov.core.FileEntity
import ru.barinov.core.Openable
import ru.barinov.core.inputStream
import ru.barinov.core.mb
import ru.barinov.file_browser.models.FileInfo
import java.io.InputStream

class MimeRecognizer {

    private val recognizerCoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    operator fun invoke(fileEntity: FileEntity, fileType: MutableState<FileInfo>) {
        recognizerCoroutineScope.launch {
            if (fileEntity.isDir) return@launch
            if (fileEntity.size.value.mb() > 7) {
                fileType.value = FileInfo.Other(true)
                return@launch
            }
            val is1 = (fileEntity as? Openable)?.inputStream() ?: return@launch
            val is2 = (fileEntity as? Openable)?.inputStream() ?: return@launch
            if (is1.isImage()) {
                is2.use {
                    it.getBitMapPreview()?.let { bitmap ->
                        fileType.value = FileInfo.ImageFile(bitmap)
                    }
                }
            } else {
                fileType.value = FileInfo.Other(false)
            }
        }
    }
}

private fun InputStream.getBitMapPreview(): Bitmap? = runCatching {
    BitmapFactory.decodeStream(this)?.let { Bitmap.createScaledBitmap(it, 100, 100, true) }
}.getOrNull()

private fun InputStream.isImage(): Boolean = runCatching {
    val bitmapOptions = BitmapFactory.Options().also { it.inJustDecodeBounds = true }
    BitmapFactory.decodeStream(this, null, bitmapOptions)
    return (bitmapOptions.outWidth != -1 && bitmapOptions.outHeight != -1).also { close() }
}.getOrNull() ?: false
