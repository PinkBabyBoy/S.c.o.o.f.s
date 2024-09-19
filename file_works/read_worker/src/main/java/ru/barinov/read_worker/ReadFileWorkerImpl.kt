package ru.barinov.read_worker

import me.jahnen.libaums.core.fs.UsbFileStreamFactory
import ru.barinov.core.FileEntity
import ru.barinov.core.FileIndex
import ru.barinov.external_data.GetMSDFileSystemUseCase
import ru.barinov.file_works.IndexCreator
import java.io.BufferedInputStream
import java.io.FileInputStream
import java.io.InputStream
import java.lang.Exception
import java.lang.IllegalArgumentException
import java.nio.ByteBuffer

private const val KEY_FILE_LIMIT_SIZE = 4*1024*1024

internal class ReadFileWorkerImpl(
    private val getMSDFileSystemUseCase: GetMSDFileSystemUseCase
): ReadFileWorker {

    override fun readIndexes(file: FileEntity.InternalFile): Result<List<FileIndex>> = runCatching{
//        if(!file.isInternalFile) throw IllegalArgumentException("Can not load from msd device file")
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

    override fun getInputStream(fileEntity: FileEntity): InputStream =
       when(fileEntity){
           is FileEntity.InternalFile -> readKeyFromInternalStorage(fileEntity)
           is FileEntity.MassStorageFile -> readKeyFromMSD(fileEntity)
       }

    private fun readKeyFromMSD(fileEntity: FileEntity.MassStorageFile): InputStream =
        UsbFileStreamFactory.createBufferedInputStream(fileEntity.attachedOrigin, getMSDFileSystemUseCase()!!)

    private fun readKeyFromInternalStorage(fileEntity:FileEntity.InternalFile): InputStream {
        if(fileEntity.attachedOrigin.length() > KEY_FILE_LIMIT_SIZE) throw Exception()
        return fileEntity.attachedOrigin.inputStream()
    }
}