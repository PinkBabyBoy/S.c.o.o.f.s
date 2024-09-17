package ru.barinov.core

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import me.jahnen.libaums.core.fs.UsbFile
import me.jahnen.libaums.core.fs.UsbFileInputStream
import me.jahnen.libaums.core.fs.UsbFileOutputStream
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID


//Вынести в домейн

@JvmInline
value class Filename(val value: String)

@JvmInline
value class Filepath(val value: String) {

    companion object {
        fun root(name: String) = Filepath(name)
    }
}

@JvmInline
value class FileSize(val value: Long)

sealed class FileEntity(
    val uuid: UUID,
    val size: FileSize,
    val isDir: Boolean,
    val name: Filename,
    val path: Filepath,
    val parent: FileEntity?
) {

    abstract fun innerFiles(): List<FileEntity>

    class MassStorageFile internal constructor(
        val attachedOrigin: UsbFile,
    ) : FileEntity(
        UUID.randomUUID(),
        FileSize(attachedOrigin.length),
        attachedOrigin.isDirectory,
        Filename(attachedOrigin.name),
        Filepath(attachedOrigin.absolutePath),
        attachedOrigin.parent?.toFileEntity()
    ) {

        override fun innerFiles(): List<MassStorageFile> =
            attachedOrigin.listFiles().map { it.toFileEntity() }
    }

    class InternalFile internal constructor(
        val attachedOrigin: File
    ) : FileEntity(
        UUID.randomUUID(),
        FileSize(attachedOrigin.length()),
        attachedOrigin.isDirectory,
        Filename(attachedOrigin.name),
        Filepath(attachedOrigin.path),
        attachedOrigin.parentFile?.toFileEntity()
    ) {

        override fun innerFiles(): List<InternalFile> =
            attachedOrigin.listFiles()?.map { it.toFileEntity() }.orEmpty()
    }

}

sealed interface FileFormatType {

    class Image(val bitmapPreview: Bitmap) : FileFormatType

    class Other(val isBigFile: Boolean) : FileFormatType
}

fun File.toFileEntity(): FileEntity.InternalFile =
    FileEntity.InternalFile(this)


fun UsbFile.toFileEntity(): FileEntity.MassStorageFile =
    FileEntity.MassStorageFile(this)

fun FileEntity.inputStream(): InputStream {
    if (isDir) error("This is folder!")
    return when(this){
        is FileEntity.InternalFile -> attachedOrigin.inputStream()
        is FileEntity.MassStorageFile -> UsbFileInputStream(attachedOrigin)
    }
}

fun FileEntity.outputStream(): OutputStream {
    if (isDir) error("This is folder!")
    return when(this){
        is FileEntity.InternalFile -> attachedOrigin.outputStream()
        is FileEntity.MassStorageFile -> UsbFileOutputStream(attachedOrigin)
    }
}



