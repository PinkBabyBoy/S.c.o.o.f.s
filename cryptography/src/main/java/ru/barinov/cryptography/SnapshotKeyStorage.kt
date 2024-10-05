package ru.barinov.cryptography

interface SnapshotKeyStorage {

    fun encrypt(hash: ByteArray): ByteArray

    fun decrypt(encryptedHash: ByteArray): ByteArray
}
