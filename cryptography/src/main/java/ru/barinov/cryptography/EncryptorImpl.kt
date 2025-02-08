package ru.barinov.cryptography

import android.util.Log
import ru.barinov.core.getBytes
import ru.barinov.cryptography.factories.CipherFactory
import ru.barinov.cryptography.keygens.SecretKeyGenerator
import javax.crypto.SecretKey

internal class EncryptorImpl(
    private val cipherFactory: CipherFactory,
    private val keyGenerator: SecretKeyGenerator
) : Encryptor {

    private fun encryptSyncBlockKey(blockKey: SecretKey): ByteArray {
        val envelopeWrapper = cipherFactory.createEnvelopeWrapperCipher()
        return envelopeWrapper.wrap(blockKey)
    }

    override fun encryptIndex(indexRaw: ByteArray): ByteArray {
        val key = keyGenerator.generateNewSecretKey()
        val innerCipher = cipherFactory.createEncryptionInnerCipherBC(key)
        val indexWrappedKey = encryptSyncBlockKey(key)
        val encIndex = innerCipher.doFinal(indexRaw)
        return indexWrappedKey.size.getBytes() + indexWrappedKey +  encIndex.size.getBytes() + encIndex
    }
}