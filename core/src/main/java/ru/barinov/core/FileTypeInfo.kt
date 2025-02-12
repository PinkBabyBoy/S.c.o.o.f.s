package ru.barinov.core

import android.graphics.Bitmap

sealed interface FileTypeInfo {
    data object Unconfirmed : FileTypeInfo
    class Other(val bigFile: Boolean, val size: String) : FileTypeInfo
    class ImageFile(val bitmapPreview: Bitmap, val size: String) : FileTypeInfo
    class Dir(val contentText: String, val count: Int) : FileTypeInfo
    class IndexStorage(val creationDate: String) : FileTypeInfo
}