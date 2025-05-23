package ru.barinov.write_worker

import android.util.Log
import me.jahnen.libaums.core.fs.UsbFileStreamFactory
import ru.barinov.core.FileEntity
import ru.barinov.core.getBytes
import ru.barinov.cryptography.Encryptor
import ru.barinov.cryptography.factories.CipherFactory
import ru.barinov.cryptography.factories.CipherStreamsFactory
import ru.barinov.cryptography.keygens.SecretKeyGenerator
import ru.barinov.external_data.GetMSDFileSystemUseCase
import ru.barinov.file_prober.IndexTypeExtractor
import ru.barinov.file_works.IndexCreator
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import javax.crypto.Cipher

internal class WriteFileWorkerImpl(
    private val cipherFactory: CipherFactory,
    private val keygen: SecretKeyGenerator,
    private val encryptor: Encryptor,
    private val cipherStreamsFactory: CipherStreamsFactory,
    private val getMSDFileSystemUseCase: GetMSDFileSystemUseCase,
    private val indexTypeExtractor: IndexTypeExtractor
) : WriteFileWorker {

    override suspend fun putInStorage(
        targetFile: FileEntity,
        progressCallback: suspend (Long) -> Unit,
        indexes: File,
        container: File
    ) {
        val index = IndexCreator.createIndex(
            targetFile, indexes.length(), container.length(), indexTypeExtractor.getTypeDirectly(targetFile)
        ).let(encryptor::encryptIndex)
        val key = keygen.generateNewSecretKey()
        val envelopeCipher = cipherFactory.createEnvelopeWrapperCipher()
        val blockCipher = cipherFactory.createEncryptionInnerCipherBC(key)
        val sizeCipher = cipherFactory.createEncryptionInnerCipherBC(key)
        val encKey = envelopeCipher.wrap(key)
        container.appendBytes(encKey.size.getBytes())
        container.appendBytes(encKey)
        when (targetFile) {
            is FileEntity.InternalFile ->
                appendInternalFile(targetFile, container, progressCallback, blockCipher, sizeCipher)

            is FileEntity.MassStorageFile ->
                appendMSDFile(targetFile, container, progressCallback, blockCipher, sizeCipher)

            is FileEntity.IndexStorage -> error("Container is not allowed here")
        }
        runCatching {
            indexes.appendBytes(index.size.getBytes() + index) // total size + size of wrappedKey + wrappedKey + size of index + index
        }.onFailure { throw IndexCreationException() }
    }

    override suspend fun deleteEntries(innerFolder: File, names: List<String>) {
        names.map { name ->
            File(innerFolder, name)
        }.forEach { file ->
            if (file.exists()) file.delete()
        }
    }

    private suspend fun appendMSDFile(
        targetFile: FileEntity.MassStorageFile,
        container: File,
        progressCallback: suspend (Long) -> Unit,
        cipher: Cipher,
        sizeCipher: Cipher
    ) {
        UsbFileStreamFactory.createBufferedInputStream(
            targetFile.attachedOrigin,
            getMSDFileSystemUseCase()!!
        ).use { input ->
            cipherStreamsFactory.createOutputStream(container.outputStream(), cipher)
                .use { output ->
                    input.copyWithProgress(output) { progressCallback.invoke(it) }
                }
        }
    }

    private suspend fun appendInternalFile(
        targetFile: FileEntity.InternalFile,
        container: File,
        progressCallback: suspend (Long) -> Unit,
        mainCipher: Cipher,
        sizeCipher: Cipher
    ) {
        val precalculatedSize = targetFile.attachedOrigin.length()
        val pLoadSize = sizeCipher.doFinal(precalculatedSize.getBytes())
        container.appendBytes(pLoadSize.size.getBytes())
        container.appendBytes(pLoadSize)
        cipherStreamsFactory.createOutputStream(FileOutputStream(container, true), mainCipher).use { output ->
            targetFile.attachedOrigin.inputStream().use { input ->
                input.copyWithProgress(output) { progressCallback.invoke(it) }
            }
        }
    }
}

private inline fun InputStream.copyWithProgress(
    out: OutputStream,
    progressListener: (Long) -> Unit
) {
    var bytesCopied: Long = 0
    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
    var bytes = read(buffer)
    while (bytes >= 0) {
        out.write(buffer, 0, bytes)
        bytesCopied += bytes
        //TODO do light pre calculation to make sure flow does not emmit to much
        progressListener(bytesCopied)
        bytes = read(buffer)
    }
}

class IndexCreationException() : Exception()
