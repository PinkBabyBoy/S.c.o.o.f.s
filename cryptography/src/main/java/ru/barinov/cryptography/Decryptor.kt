package ru.barinov.cryptography

interface Decryptor {

    suspend fun decryptIndex(encryptedKey: ByteArray, rawIndex: ByteArray): ByteArray
}
