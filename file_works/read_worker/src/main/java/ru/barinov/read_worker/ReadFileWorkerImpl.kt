package ru.barinov.read_worker

import me.jahnen.libaums.core.fs.UsbFileStreamFactory
import ru.barinov.core.FileEntity
import ru.barinov.core.FileIndex
import ru.barinov.cryptography.Decryptor
import ru.barinov.external_data.GetMSDFileSystemUseCase
import ru.barinov.file_works.IndexCreator
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.lang.Exception
import java.nio.ByteBuffer

private const val KEY_FILE_LIMIT_SIZE = 4*1024*1024

internal class ReadFileWorkerImpl(
    private val getMSDFileSystemUseCase: GetMSDFileSystemUseCase,
    private val decryptor: Decryptor
): ReadFileWorker {

    override suspend fun readIndexes(file: File): Result<List<FileIndex>> = runCatching{
        FileInputStream(file).buffered().use { iStream ->
            var pos:Long = 0
            mutableListOf<FileIndex>().apply {
                while (iStream.available() > 0) {
                    val tagSize = readSize(iStream)
                    ByteArray(tagSize).also { tagBuffer ->
                        val startPos = pos
                        pos += iStream.read(tagBuffer) + Int.SIZE_BYTES
                        add(IndexCreator.restoreIndex(decodeIndex(tagBuffer), startPos, tagSize))
                    }
                }
            }
        }
    }

    private suspend fun decodeIndex(encodedIndex: ByteArray): ByteArray {
        val indexStream = encodedIndex.inputStream()
        val keySize = ByteBuffer.wrap(ByteArray(Int.SIZE_BYTES).also { indexStream.read(it) }).getInt()
        val encryptedKey = ByteBuffer.wrap(ByteArray(keySize).also { indexStream.read(it) }).array()
        return decryptor.decryptIndex(encryptedKey, indexStream.readBytes())
    }

    private fun readSize(input: InputStream): Int {
        val buffer = ByteArray(Int.SIZE_BYTES)
        input.read(buffer)
        return ByteBuffer.wrap(buffer).getInt()
    }

    private fun readKeyFromMSD(fileEntity: FileEntity.MassStorageFile): InputStream =
        UsbFileStreamFactory.createBufferedInputStream(fileEntity.attachedOrigin, getMSDFileSystemUseCase()!!)

    private fun readKeyFromInternalStorage(fileEntity:FileEntity.InternalFile): InputStream {
        if(fileEntity.attachedOrigin.length() > KEY_FILE_LIMIT_SIZE) throw Exception()
        return fileEntity.attachedOrigin.inputStream()
    }
}