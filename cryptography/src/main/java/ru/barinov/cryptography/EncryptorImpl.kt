package ru.barinov.cryptography

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
        val innerCipher = cipherFactory.createEncryptionInnerCipher(key)
        //todo indexSize
        return encryptSyncBlockKey(key) + innerCipher.doFinal(indexRaw)
    }
}