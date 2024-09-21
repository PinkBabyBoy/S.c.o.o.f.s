package ru.barinov.core

import me.jahnen.libaums.core.fs.UsbFile
import me.jahnen.libaums.core.fs.UsbFileInputStream
import me.jahnen.libaums.core.fs.UsbFileOutputStream
import java.io.File
import java.io.InputStream
import java.io.OutputStream


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
    val lastModifiedTimeStamp: Long,
    val fileId: FileId,
    val size: FileSize,
    val isDir: Boolean,
    val name: Filename,
    val path: Filepath,
    val parent: FileEntity?
) {

    abstract fun innerFiles(): Map<FileId, FileEntity>

    class MassStorageFile internal constructor(
        val attachedOrigin: UsbFile,
    ) : FileEntity(
        attachedOrigin.lastModified(),
        FileId.byFilePath(Filepath(attachedOrigin.absolutePath)),
        FileSize(if (!attachedOrigin.isDirectory) attachedOrigin.length else 0L),
        attachedOrigin.isDirectory,
        Filename(attachedOrigin.name),
        Filepath(attachedOrigin.absolutePath),
        attachedOrigin.parent?.toFileEntity()
    ) {

        override fun innerFiles(): Map<FileId, FileEntity> = runCatching {
            attachedOrigin.listFiles().associate {
                val entity = it.toFileEntity()
                return@associate entity.fileId to entity
            }
        }.getOrNull() ?: emptyMap()

    }

    class InternalFile internal constructor(
        val attachedOrigin: File
    ) : FileEntity(
        attachedOrigin.lastModified(),
        FileId.byFilePath(Filepath(attachedOrigin.absolutePath)),
        FileSize(attachedOrigin.length()),
        attachedOrigin.isDirectory,
        Filename(attachedOrigin.name),
        Filepath(attachedOrigin.path),
        attachedOrigin.parentFile?.toFileEntity()
    ) {

        override fun innerFiles(): Map<FileId, FileEntity> = runCatching {
            attachedOrigin.listFiles()?.associate {
                val entity = it.toFileEntity()
                return@associate entity.fileId to entity
            }
        }.getOrNull() ?: emptyMap()
    }

}

@JvmInline
value class FileId private constructor(val path: String) {

    companion object {
        fun byFilePath(filepath: Filepath): FileId {
            return FileId(filepath.value)
        }
    }
}

fun File.toFileEntity(): FileEntity.InternalFile =
    FileEntity.InternalFile(this)


fun UsbFile.toFileEntity(): FileEntity.MassStorageFile =
    FileEntity.MassStorageFile(this)

fun FileEntity.inputStream(): InputStream {
    if (isDir) error("This is folder!")
    return when (this) {
        is FileEntity.InternalFile -> attachedOrigin.inputStream()
        is FileEntity.MassStorageFile -> UsbFileInputStream(attachedOrigin)
    }
}

fun FileEntity.outputStream(): OutputStream {
    if (isDir) error("This is folder!")
    return when (this) {
        is FileEntity.InternalFile -> attachedOrigin.outputStream()
        is FileEntity.MassStorageFile -> UsbFileOutputStream(attachedOrigin)
    }
}



