package ru.barinov.cryptography.factories

import javax.crypto.Cipher
import javax.crypto.SecretKey

interface CipherFactory {

    //TODO IV?
    fun createDecryptionInnerCipher(rawSecretKey: ByteArray, iv: ByteArray? = null): Cipher

    fun createDecryptionInnerCipher(key: SecretKey, iv: ByteArray?): Cipher

    fun createEncryptionInnerCipher(key: SecretKey): Cipher

    fun createEnvelopeWrapperCipher(): Cipher

    fun createEnvelopeUnWrapperCipher(): Cipher
}