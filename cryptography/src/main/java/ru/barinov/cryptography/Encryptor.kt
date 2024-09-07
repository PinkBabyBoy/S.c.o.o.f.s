package ru.barinov.cryptography

import javax.crypto.SecretKey

interface Encryptor {

    fun encryptIndex(indexRaw: ByteArray): ByteArray
}
