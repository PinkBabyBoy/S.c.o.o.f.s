package ru.barinov.cryptography


interface Encryptor {

    fun encryptIndex(indexRaw: ByteArray): ByteArray
}

