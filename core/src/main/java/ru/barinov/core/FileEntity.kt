package ru.barinov.core

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import me.jahnen.libaums.core.fs.UsbFile
import me.jahnen.libaums.core.fs.UsbFileInputStream
import me.jahnen.libaums.core.fs.UsbFileOutputStream
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.io.Serializable

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

sealed interface Addable {
    val isDir: Boolean
    val parent: Addable?
    val path: Filepath
    suspend fun innerFilesAsync(): Map<FileId, FileEntity>
    fun innerFiles(): Map<FileId, FileEntity>
}

sealed class FileEntity(
    val lastModifiedTimeStamp: Long,
    val fileId: FileId,
    val size: FileSize,
    val isDir: Boolean,
    val name: Filename,
    val path: Filepath,
    val parent: Addable?
) {

    abstract fun containsCount(): Int?

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
    ), Addable {

        override fun innerFiles(): Map<FileId, FileEntity> = runCatching {
            attachedOrigin.listFiles().map { it.toInternalFileEntity() }.associateBy { it.fileId }
        }.getOrNull() ?: emptyMap()

        override suspend fun innerFilesAsync(): Map<FileId, FileEntity> = coroutineScope {
            runCatching {
                attachedOrigin.listFiles().map {
                    async { it.toInternalFileEntity() }
                }.map { it.await() }.associateBy { it.fileId }
            }.getOrNull().orEmpty()
        }

        override fun containsCount(): Int = attachedOrigin.list().size
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
    ), Addable {

        override fun innerFiles(): Map<FileId, FileEntity> = runCatching {
            attachedOrigin.listFiles()?.map { it.toInternalFileEntity() }?.associateBy { it.fileId }
        }.getOrNull() ?: emptyMap()

        override suspend fun innerFilesAsync(): Map<FileId, FileEntity> = coroutineScope {
            runCatching {
                attachedOrigin.listFiles()?.map {
                    async { it.toInternalFileEntity() }
                }?.map { it.await() }?.associateBy { it.fileId }.orEmpty()
            }.getOrNull().orEmpty()
        }

        override fun containsCount(): Int? {
           return attachedOrigin.list()?.size
        }
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
    ) {
        override fun containsCount(): Int {
            TODO("Not yet implemented")
        }
    }
}

@JvmInline
value class FileId private constructor(val value: String): Serializable {

    companion object {
        fun byFilePath(filepath: Filepath): FileId {
            return FileId(filepath.value)
        }

        fun byName(name: String): FileId {
            return FileId(name)
        }

        fun restore(fileId: String) = FileId(fileId)
    }
}

fun File.toInternalFileEntity(): FileEntity.InternalFile =
    FileEntity.InternalFile(this)

fun File.toContainerFileEntity(): FileEntity.Index =
    FileEntity.Index(this)


fun UsbFile.toInternalFileEntity(): FileEntity.MassStorageFile =
    FileEntity.MassStorageFile(this)

fun Addable.inputStream(): InputStream {
    return when (this) {
        is FileEntity.InternalFile -> attachedOrigin.inputStream()
        is FileEntity.MassStorageFile -> UsbFileInputStream(attachedOrigin)
    }
}

fun Addable.outputStream(): OutputStream {
    return when (this) {
        is FileEntity.InternalFile -> attachedOrigin.outputStream()
        is FileEntity.MassStorageFile -> UsbFileOutputStream(attachedOrigin)
    }
}
