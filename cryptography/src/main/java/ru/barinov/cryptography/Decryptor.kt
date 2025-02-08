package ru.barinov.cryptography

import java.nio.ByteBuffer

interface Decryptor {

    suspend fun decryptIndex(encryptedKey: ByteArray, rawIndex: ByteArray): ByteArray
}
