package ru.barinov.file_works

import kotlinx.coroutines.flow.MutableSharedFlow
import me.jahnen.libaums.core.fs.UsbFileStreamFactory
import ru.barinov.core.FileEntity
import ru.barinov.cryptography.Encryptor
import ru.barinov.cryptography.factories.CipherFactory
import ru.barinov.cryptography.factories.CipherStreamsFactory
import ru.barinov.cryptography.keygens.SecretKeyGenerator
import ru.barinov.external_data.GetMSDFileSystemUseCase
import ru.barinov.filework.WriteFileWorker
import ru.barinov.filework.getBytes
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import javax.crypto.Cipher

//private const val PASS_FILE_NAME = "volt"

internal class WriteFileWorkerImpl(
    private val cipherFactory: CipherFactory,
    private val keygen: SecretKeyGenerator,
    private val encryptor: Encryptor,
    private val cipherStreamsFactory: CipherStreamsFactory,
    private val getMSDFileSystemUseCase: GetMSDFileSystemUseCase
) : WriteFileWorker {



    override suspend fun putInStorage(
        targetFile: FileEntity,
        progressFlow: MutableSharedFlow<Long>?,
        indexes: File,
        container: File
    ) {
        val index = IndexCreator.createIndex(
            targetFile, indexes.length(), container.length()
        ).let(encryptor::encryptIndex)
        val key = keygen.generateNewSecretKey()
        val envelopeCipher = cipherFactory.createEnvelopeWrapperCipher()
        val blockCipher = cipherFactory.createEncryptionInnerCipher(key)
        val encKey = envelopeCipher.wrap(key)
        container.appendBytes(encKey.size.getBytes())
        container.appendBytes(encKey)
        when (targetFile) {
            is FileEntity.InternalFile ->
                appendInternalFile(targetFile, container, progressFlow, blockCipher)

            is FileEntity.MassStorageFile ->
                appendMSDFile(targetFile, container, progressFlow, blockCipher)
        }
        runCatching {
            indexes.appendBytes(index.size.getBytes())
            indexes.appendBytes(index)
        }.onFailure { throw IndexCreationException() }
    }

    override suspend fun deleteEntries(innerFolder: File, names: List<String>) {
        names.map { name ->
            File(innerFolder, name)
        }.forEach { file ->
            if(file.exists()) file.delete()
        }
    }

    private suspend fun appendMSDFile(
        targetFile: FileEntity.MassStorageFile,
        container: File,
        progressFlow: MutableSharedFlow<Long>?,
        cipher: Cipher
    ) {
        UsbFileStreamFactory.createBufferedInputStream(
            targetFile.attachedOrigin,
            getMSDFileSystemUseCase()!!
        ).use { input ->
            cipherStreamsFactory.createOutputStream(container.outputStream(), cipher)
                .use { output ->
                    input.copyWithProgress(output) { progressFlow?.tryEmit(it) }
                }
        }
    }

    private suspend fun appendInternalFile(
        targetFile: FileEntity.InternalFile,
        container: File,
        progressFlow: MutableSharedFlow<Long>?,
        cipher: Cipher
    ) {
        targetFile.attachedOrigin.inputStream().use { input ->
            cipherStreamsFactory.createOutputStream(container.outputStream(), cipher)
                .use { output ->
                    input.copyWithProgress(output) { progressFlow?.tryEmit(it) }
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
