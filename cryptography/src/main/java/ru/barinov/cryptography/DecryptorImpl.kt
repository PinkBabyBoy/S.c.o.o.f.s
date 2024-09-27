package ru.barinov.cryptography

import ru.barinov.cryptography.factories.CipherFactory

internal class DecryptorImpl(
    private val cipherFactory: CipherFactory
): Decryptor {


    override suspend fun decryptIndex(encryptedKey: ByteArray, rawIndex: ByteArray): ByteArray =
        cipherFactory.createDecryptionInnerCipher(encryptedKey).doFinal(rawIndex)
}
