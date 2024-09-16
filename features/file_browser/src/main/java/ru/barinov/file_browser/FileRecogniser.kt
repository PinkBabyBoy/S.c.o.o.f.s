package ru.barinov.file_browser

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.MutableState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import ru.barinov.core.FileEntity
import ru.barinov.core.inputStream
import ru.barinov.core.mb
import ru.barinov.file_browser.models.FileType
import java.io.InputStream

class FileRecogniser {

    private val recognizerCoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    operator fun invoke(fileEntity: FileEntity, fileType: MutableState<FileType>){
        recognizerCoroutineScope.launch {
            if (fileEntity.isDir) return@launch
            if (fileEntity.size.value.mb() > 7) {
                fileType.value = FileType.Other(true)
                return@launch
            }
            val is1 = fileEntity.inputStream()
            val is2 = fileEntity.inputStream()
            if (is1.isImage()) {
                is2.use {
                    it.getBitMapPreview()?.let { bitmap ->
                        fileType.value = FileType.ImageFile(bitmap)
                    }
                }
            } else {
                fileType.value = FileType.Other(false)
            }
        }
    }
}

private fun InputStream.getBitMapPreview(): Bitmap? =
    BitmapFactory.decodeStream(this)?.let { Bitmap.createScaledBitmap(it, 42, 42, true) }

private fun InputStream.isImage(): Boolean {
    val bitmapOptions = BitmapFactory.Options().also { it.inJustDecodeBounds = true }
    BitmapFactory.decodeStream(this, null, bitmapOptions)
    return (bitmapOptions.outWidth != -1 && bitmapOptions.outHeight != -1).also { close() }
}