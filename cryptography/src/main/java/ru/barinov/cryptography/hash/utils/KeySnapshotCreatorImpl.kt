package ru.barinov.cryptography.hash.utils

import ru.barinov.cryptography.SnapshotKeyStorage
import ru.barinov.cryptography.hash.HashCreator
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.PublicKey

internal class KeySnapshotCreatorImpl(
    private val snapshotKeyStorage: SnapshotKeyStorage,
    private val hashCreator: HashCreator
): KeySnapshotCreator {

    override fun createFrom(public: PublicKey, private: PrivateKey): ByteArray =
        snapshotKeyStorage.encrypt(hashCreator.createHash(public.encoded) + hashCreator.createHash(private.encoded))
}

fun interface KeySnapshotCreator{

    fun createFrom(public: PublicKey, private: PrivateKey): ByteArray
}
