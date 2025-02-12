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

//TODO To Domain

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

sealed interface InteractableFile {
    val isDir: Boolean
    val parent: InteractableFile?
    val path: Filepath
    suspend fun innerFilesAsync(): Map<FileId, FileEntity>
    fun innerFiles(): Map<FileId, FileEntity>
}

sealed interface EncryptedData

sealed class FileEntity(
    val lastModifiedTimeStamp: Long,
    val fileId: FileId,
    val size: FileSize,
    val isDir: Boolean,
    val name: Filename,
    val path: Filepath,
    val parent: InteractableFile?
): StorageAble {

    abstract suspend fun calculateSize(): Long

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
    ), InteractableFile {

        override fun innerFiles(): Map<FileId, FileEntity> = runCatching {
            attachedOrigin.listFiles().map { it.toInternalFileEntity() }.associateBy { it.fileId }
        }.getOrNull() ?: emptyMap()

        override suspend fun calculateSize(): Long =
            if(isDir) attachedOrigin.listFiles().sumOf { it.length }
            else size.value

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
    ), InteractableFile {

        override fun innerFiles(): Map<FileId, FileEntity> = runCatching {
            attachedOrigin.listFiles()?.map { it.toInternalFileEntity() }?.associateBy { it.fileId }
        }.getOrNull() ?: emptyMap()

        override suspend fun calculateSize(): Long =
            if(isDir) attachedOrigin.listFiles()?.sumOf { it.length() } ?: 0L
            else size.value

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

    class IndexStorage internal constructor(
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
        override suspend fun calculateSize(): Long {
            TODO("Not yet implemented")
        }

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

        fun byPointer(pointer: Long): FileId {
            return FileId(pointer.toString())
        }

        fun restore(fileId: String) = FileId(fileId)
    }
}

fun File.toInternalFileEntity(): FileEntity.InternalFile =
    FileEntity.InternalFile(this)

fun File.toContainerFileEntity(): FileEntity.IndexStorage =
    FileEntity.IndexStorage(this)


fun UsbFile.toInternalFileEntity(): FileEntity.MassStorageFile =
    FileEntity.MassStorageFile(this)

fun InteractableFile.inputStream(): InputStream {
    return when (this) {
        is FileEntity.InternalFile -> attachedOrigin.inputStream()
        is FileEntity.MassStorageFile -> UsbFileInputStream(attachedOrigin)
    }
}

fun InteractableFile.outputStream(): OutputStream {
    return when (this) {
        is FileEntity.InternalFile -> attachedOrigin.outputStream()
        is FileEntity.MassStorageFile -> UsbFileOutputStream(attachedOrigin)
    }
}

sealed interface StorageAble
