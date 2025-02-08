package ru.barinov.read_worker

import android.util.Log
import me.jahnen.libaums.core.fs.UsbFileStreamFactory
import ru.barinov.core.FileEntity
import ru.barinov.core.FileIndex
import ru.barinov.cryptography.Decryptor
import ru.barinov.external_data.GetMSDFileSystemUseCase
import ru.barinov.file_works.IndexCreator
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
        if(file.length() < 1) return@runCatching emptyList()
        FileInputStream(file).use { iStream ->
            iStream.skipHash()
            var pos:Long = 0
            mutableListOf<FileIndex>().apply {
                while (iStream.available() > 0) {
                    val indexSize = iStream.readSize()
                    ByteArray(indexSize).also { indexBuffer ->
                        val startPos = pos
                        pos += iStream.read(indexBuffer) + Int.SIZE_BYTES
                        add(IndexCreator.restoreIndex(decodeIndex(indexBuffer, indexSize), startPos, indexSize))
                    }
                }
            }
        }
    }

    private fun InputStream.skipHash() {
        val hashSize = readSize()
        skip(hashSize.toLong())
    }

    private fun InputStream.readSize(): Int {
        val buffer = ByteArray(Int.SIZE_BYTES).apply(::read)
        return ByteBuffer.wrap(buffer).getInt()
    }

    private suspend fun decodeIndex(encodedIndex: ByteArray, totalSize: Int): ByteArray =
//        indexes.appendBytes(index.size.getBytes() + index) // total size + size of wrappedKey + wrappedKey + size of index + index
        ByteBuffer.wrap(encodedIndex).run {
            val wKeySize = getInt()
            val wrappedKey = ByteArray(wKeySize).apply(::get)
            val encPayload = ByteArray(getInt()).apply(::get)
            decryptor.decryptIndex(wrappedKey, encPayload)
        }
//        val indexStream = encodedIndex.inputStream()
//        val keySize = ByteBuffer.wrap(ByteArray(Int.SIZE_BYTES).also { indexStream.read(it) }).getInt()
//        val encryptedKey = ByteBuffer.wrap(ByteArray(keySize).also { indexStream.read(it) }).array()
//        return decryptor.decryptIndex(encryptedKey, indexStream.readBytes())
//    }



    private fun readKeyFromMSD(fileEntity: FileEntity.MassStorageFile): InputStream =
        UsbFileStreamFactory.createBufferedInputStream(fileEntity.attachedOrigin, getMSDFileSystemUseCase()!!)

    private fun readKeyFromInternalStorage(fileEntity:FileEntity.InternalFile): InputStream {
        if(fileEntity.attachedOrigin.length() > KEY_FILE_LIMIT_SIZE) throw Exception()
        return fileEntity.attachedOrigin.inputStream()
    }
}