package ru.barinov.cryptography.factories

import javax.crypto.Cipher
import javax.crypto.SecretKey

interface CipherFactory {

    fun createDecryptionInnerCipher(rawSecretKey: ByteArray, iv: ByteArray): Cipher

    fun createDecryptionInnerCipher(key: SecretKey, iv: ByteArray?): Cipher

    fun createEncryptionInnerCipher(key: SecretKey): Cipher

    fun createEnvelopeWrapperCipher(): Cipher

    fun createEnvelopeUnWrapperCipher(): Cipher
}