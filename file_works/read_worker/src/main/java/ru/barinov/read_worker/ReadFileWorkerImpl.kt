package ru.barinov.read_worker

import android.os.Build
import androidx.annotation.RequiresApi
import me.jahnen.libaums.core.fs.UsbFileStreamFactory
import ru.barinov.core.FileEntity
import ru.barinov.core.FileIndex
import ru.barinov.cryptography.Decryptor
import ru.barinov.external_data.GetMSDFileSystemUseCase
import ru.barinov.file_works.IndexCreator
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.RandomAccessFile
import java.lang.Exception
import java.nio.ByteBuffer

private const val KEY_FILE_LIMIT_SIZE = 4 * 1024 * 1024

internal class ReadFileWorkerImpl(
    private val getMSDFileSystemUseCase: GetMSDFileSystemUseCase,
    private val decryptor: Decryptor
) : ReadFileWorker {

    @Deprecated("Use paging implementation")
    override suspend fun readIndexes(file: File, limit: Int): List<FileIndex> {
        if (file.length() < 1) return emptyList()
        return FileInputStream(file).use { iStream ->
            val hashSkipped = iStream.skipHash()
            mutableListOf<FileIndex>().apply {
                iStream.readIndex(this, limit, hashSkipped)
            }
        }
    }


    @Deprecated("Use RandomAccessFile extension")
    private suspend fun InputStream.readIndex(
        to: MutableList<FileIndex>,
        limit: Int,
        startPosition: Int
    ) {
        var pos: Long = 0
        while (available() > 0 && to.size < limit) {
            val indexSize = readSize()
            ByteArray(indexSize).also { indexBuffer ->
                val startPos = pos
                pos += read(indexBuffer) + startPosition + Int.SIZE_BYTES
                to.add(
                    IndexCreator.restoreIndex(
                        decodeIndex(indexBuffer, indexSize),
                        startPos,
                        indexSize
                    )
                )
            }
        }
    }

    private suspend fun RandomAccessFile.readIndex(
        to: MutableList<FileIndex>,
        limit: Int,
        startPosition: Long
    ) {
        var pos: Long = 0
        while ((length() - filePointer) > 0 && to.size < limit) {
            val indexSize = readSize()
            ByteArray(indexSize).also { indexBuffer ->
                val startPos = pos
                pos += readFully(indexBuffer).let { indexSize + startPosition + Int.SIZE_BYTES }
                to.add(
                    IndexCreator.restoreIndex(
                        decryptedIndex = decodeIndex(indexBuffer, indexSize),
                        startPos = startPos,
                        rawSize = indexSize
                    )
                )
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override suspend fun getIndexesByOffsetAndLimit(file: File, from: Long, limit: Int): List<FileIndex> =
        RandomAccessFile(file, "r").use { ra ->
            val resolvedOffset = ra.resolvePointer(from)
            mutableListOf<FileIndex>().apply {
                ra.readIndex(this, limit, resolvedOffset)
            }
        }

    private fun RandomAccessFile.resolvePointer(pointer: Long): Long {
        if(pointer < 0) error("Offset is less than zero is illegal")
        return pointer.takeIf { it != 0L } ?: skipHash().toLong()
    }

    private fun RandomAccessFile.skipHash(): Int{
        val hashSize = readSize()
        return skipBytes(hashSize)
    }

    private fun RandomAccessFile.readSize(): Int {
        val buffer = ByteArray(Int.SIZE_BYTES).apply(::readFully)
        return ByteBuffer.wrap(buffer).getInt()
    }

    private fun InputStream.skipHash(): Int {
        val hashSize = readSize()
        skip(hashSize.toLong())
        return Int.SIZE_BYTES + hashSize
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
        UsbFileStreamFactory.createBufferedInputStream(
            fileEntity.attachedOrigin,
            getMSDFileSystemUseCase()!!
        )

    private fun readKeyFromInternalStorage(fileEntity: FileEntity.InternalFile): InputStream {
        if (fileEntity.attachedOrigin.length() > KEY_FILE_LIMIT_SIZE) throw Exception()
        return fileEntity.attachedOrigin.inputStream()
    }
}