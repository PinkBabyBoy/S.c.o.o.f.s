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

sealed interface Openable {
    val isDir: Boolean
    val parent: Openable?
    val path: Filepath
    fun innerFiles(): Map<FileId, FileEntity>
    fun contentSize() : Int
}

sealed interface FileCategory {
    @JvmInline
    value class File(val size: FileSize) : FileCategory
    @JvmInline
    value class Directory(val filesCount: Int) : FileCategory
}

sealed class FileEntity(
    val lastModifiedTimeStamp: Long,
    val fileId: FileId,
    val size: FileSize,
    val isDir: Boolean,
    val name: Filename,
    val path: Filepath,
    val parent: Openable?
) {

    class MassStorageFile internal constructor(
        val attachedOrigin: UsbFile,
    ) : FileEntity(
        lastModifiedTimeStamp = attachedOrigin.lastModified(),
        fileId = FileId.byFilePath(Filepath(attachedOrigin.absolutePath)),
        size = FileSize(attachedOrigin.length),
        isDir = attachedOrigin.isDirectory,
        name = Filename(attachedOrigin.name),
        path = Filepath(attachedOrigin.absolutePath),
        parent = attachedOrigin.parent?.toInternalFileEntity()
    ), Openable {

        override fun innerFiles(): Map<FileId, FileEntity> = runCatching {
            attachedOrigin.listFiles().associate {
                val entity = it.toInternalFileEntity()
                return@associate entity.fileId to entity
            }
        }.getOrNull() ?: emptyMap()

        override fun contentSize(): Int = attachedOrigin.list()?.size ?: 0
    }

    class InternalFile internal constructor(
        val attachedOrigin: File
    ) : FileEntity(
        lastModifiedTimeStamp = attachedOrigin.lastModified(),
        fileId = FileId.byFilePath(Filepath(attachedOrigin.absolutePath)),
        size = FileSize(attachedOrigin.length()),
        isDir = attachedOrigin.isDirectory,
        name = Filename(attachedOrigin.name),
        path = Filepath(attachedOrigin.path),
        parent = attachedOrigin.parentFile?.toInternalFileEntity()
    ), Openable {


        override fun innerFiles(): Map<FileId, FileEntity> = runCatching {
            attachedOrigin.listFiles()?.associate {
                val entity = it.toInternalFileEntity()
                return@associate entity.fileId to entity
            }
        }.getOrNull() ?: emptyMap()

        override fun contentSize(): Int = attachedOrigin.list()?.size ?: 0
    }

    class Index internal constructor(
        val attachedOrigin: File
    ) : FileEntity(
        lastModifiedTimeStamp = attachedOrigin.lastModified(),
        fileId = FileId.byName(attachedOrigin.name),
        size = FileSize(attachedOrigin.length()),
        isDir = attachedOrigin.isDirectory,
        name = Filename(attachedOrigin.name),
        path = Filepath(attachedOrigin.path),
        parent = null
    )
}

@JvmInline
value class FileId private constructor(val path: String) {

    companion object {
        fun byFilePath(filepath: Filepath): FileId {
            return FileId(filepath.value)
        }

        fun byName(name: String): FileId {
            return FileId(name)
        }
    }
}

fun File.toInternalFileEntity(): FileEntity.InternalFile =
    FileEntity.InternalFile(this)

fun File.toContainerFileEntity(): FileEntity.Index =
    FileEntity.Index(this)


fun UsbFile.toInternalFileEntity(): FileEntity.MassStorageFile =
    FileEntity.MassStorageFile(this)

fun Openable.inputStream(): InputStream {
    return when (this) {
        is FileEntity.InternalFile -> attachedOrigin.inputStream()
        is FileEntity.MassStorageFile -> UsbFileInputStream(attachedOrigin)
    }
}

fun Openable.outputStream(): OutputStream {
    return when (this) {
        is FileEntity.InternalFile -> attachedOrigin.outputStream()
        is FileEntity.MassStorageFile -> UsbFileOutputStream(attachedOrigin)
    }
}
