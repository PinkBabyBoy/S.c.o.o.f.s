package ru.barinov.core

import me.jahnen.libaums.core.fs.UsbFile
import java.io.File
import java.util.UUID

//Вынести в домейн

@JvmInline
value class Filename(val value: String) {

    companion object{
        fun empty() = Filepath(String())
    }
}

@JvmInline
value class Filepath(val value: String) {

    companion object{
        fun empty() = Filepath(String())
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

    class MassStorageFile(
        val attachedOrigin: UsbFile
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

    class InternalFile(
        val attachedOrigin: File,
        val isInternalFile: Boolean = false
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

fun File.toFileEntity(): FileEntity.InternalFile =
    FileEntity.InternalFile(this)


fun UsbFile.toFileEntity(): FileEntity.MassStorageFile =
    FileEntity.MassStorageFile(this)
