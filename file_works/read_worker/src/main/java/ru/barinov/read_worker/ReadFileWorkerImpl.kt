package ru.barinov.read_worker

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import me.jahnen.libaums.core.fs.UsbFileStreamFactory
import ru.barinov.core.FileEntity
import ru.barinov.core.FileIndex
import ru.barinov.cryptography.Decryptor
import ru.barinov.cryptography.factories.CipherFactory
import ru.barinov.cryptography.factories.CipherStreamsFactory
import ru.barinov.external_data.GetMSDFileSystemUseCase
import ru.barinov.file_works.IndexCreator
import ru.barinov.read_worker.util.LimitedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.RandomAccessFile
import java.lang.Exception
import java.nio.ByteBuffer

private const val KEY_FILE_LIMIT_SIZE = 4 * 1024 * 1024

internal class ReadFileWorkerImpl(
    private val getMSDFileSystemUseCase: GetMSDFileSystemUseCase,
    private val decryptor: Decryptor,
    private val cipherFactory: CipherFactory,
    private val cipherStreamsFactory: CipherStreamsFactory,
) : ReadFileWorker {

    override suspend fun readFile(index: FileIndex): InputStream {
        RandomAccessFile(index.container, "r").use { ra ->
            Log.d("@@@", "C S ${index.container.length()} ${index.startPoint}")
            if (index.startPoint > 0) ra.seek(index.startPoint)
            val keySize = ra.readSize()
            val wrappedKey = ByteArray(keySize).apply(ra::readFully)
            val decryptionInnerCipher = cipherFactory.createDecryptionInnerCipher(wrappedKey)
            val pLoadSize = ra.readSize()
            val fileSize = decryptionInnerCipher.doFinal(
                ByteArray(pLoadSize).apply(ra::readFully)
            ).run { ByteBuffer.wrap(this).getLong() }
            val containerIStream = LimitedInputStream(index.container.inputStream().also {
                it.skip(index.startPoint + Int.SIZE_BYTES  + keySize + Int.SIZE_BYTES  + pLoadSize)
            }, fileSize + 16)
            return  cipherStreamsFactory.createInputStream(containerIStream, decryptionInnerCipher)
        }
    }

    @Deprecated("Use paging implementation")
    override suspend fun readIndexes(container: File, limit: Int): List<FileIndex> {
        if (container.length() < 1) return emptyList()
        return FileInputStream(container).use { iStream ->
            val hashSkipped = iStream.skipHash()
            mutableListOf<FileIndex>().apply {
                iStream.readIndex(this, limit, hashSkipped, container)
            }
        }
    }


    @Deprecated("Use RandomAccessFile extension")
    private suspend fun InputStream.readIndex(
        to: MutableList<FileIndex>,
        limit: Int,
        startPosition: Int,
        container: File
    ) {
        var pos: Long = 0
        while (available() > 0 && to.size < limit) {
            val indexSize = readSize()
            ByteArray(indexSize).also { indexBuffer ->
                val startPos = pos
                pos += read(indexBuffer) + startPosition + Int.SIZE_BYTES
                to.add(
                    IndexCreator.restoreIndex(
                        decryptedIndex = decodeIndex(indexBuffer, indexSize),
                        startPos = startPos,
                        rawSize = indexSize,
                        container = container
                    )
                )
            }
        }
    }

    private suspend fun RandomAccessFile.readIndex(
        to: MutableList<FileIndex>,
        limit: Int,
        startPosition: Long,
        container: File
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
                        rawSize = indexSize,
                        container = container
                    )
                )
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override suspend fun getIndexesByOffsetAndLimit(indexes: File, container: File, from: Long, limit: Int): List<FileIndex> =
        RandomAccessFile(indexes, "r").use { ra ->
            val resolvedOffset = ra.resolvePointer(from)
            mutableListOf<FileIndex>().apply {
                ra.readIndex(this, limit, resolvedOffset, container)
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

    private fun RandomAccessFile.readLongSize(): Long {
        val buffer = ByteArray(Long.SIZE_BYTES).apply(::readFully)
        return ByteBuffer.wrap(buffer).getLong()
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