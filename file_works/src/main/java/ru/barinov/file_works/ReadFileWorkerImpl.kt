package ru.barinov.file_works

import me.jahnen.libaums.core.fs.UsbFileStreamFactory
import ru.barinov.core.FileEntity
import ru.barinov.core.FileIndex
import ru.barinov.external_data.GetMSDFileSystemUseCase
import ru.barinov.filework.ReadFileWorker
import java.io.BufferedInputStream
import java.io.FileInputStream
import java.lang.Exception
import java.lang.IllegalArgumentException
import java.nio.ByteBuffer

private const val KEY_FILE_LIMIT_SIZE = 4*1024*1024

internal class ReadFileWorkerImpl(
    private val getMSDFileSystemUseCase: GetMSDFileSystemUseCase
): ReadFileWorker {

    override fun readIndexes(file: FileEntity.InternalFile): Result<List<FileIndex>> = runCatching{
        if(!file.isInternalFile) throw IllegalArgumentException("Can not load from msd device file")
        val tagFile = file.attachedOrigin
        FileInputStream(tagFile).buffered().use { iStream ->
            var pos:Long = 0
            mutableListOf<FileIndex>().apply {
                while (iStream.available() > 0) {
                    val tagSize = readTagSize(iStream)
                    ByteArray(tagSize).let { tagBuffer ->
                        val startPos = pos
                        pos += iStream.read(tagBuffer) + Int.SIZE_BYTES
                        IndexCreator.restoreIndex(tagBuffer, startPos)
                    }
                }
            }
        }
    }

    private fun readTagSize(dataFlow: BufferedInputStream): Int {
        val buffer = ByteArray(Int.SIZE_BYTES)
        dataFlow.read(buffer)
        return ByteBuffer.wrap(buffer).getInt()
    }

    override fun readRawKey(fileEntity: FileEntity): ByteArray =
       when(fileEntity){
           is FileEntity.InternalFile -> readKeyFromInternalStorage(fileEntity)
           is FileEntity.MassStorageFile -> readKeyFromMSD(fileEntity)
       }

    private fun readKeyFromMSD(fileEntity: FileEntity.MassStorageFile): ByteArray =
        UsbFileStreamFactory.createBufferedInputStream(fileEntity.attachedOrigin, getMSDFileSystemUseCase()!!).use {
            if(it.available() > KEY_FILE_LIMIT_SIZE) throw Exception()
            it.readBytes()
        }

    private fun readKeyFromInternalStorage(fileEntity: FileEntity.InternalFile): ByteArray {
        if(fileEntity.attachedOrigin.length() > KEY_FILE_LIMIT_SIZE) throw Exception()
        return fileEntity.attachedOrigin.readBytes()
    }
}
