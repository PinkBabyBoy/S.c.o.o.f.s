package ru.barinov.cryptography.factories

import java.io.InputStream
import java.io.OutputStream
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream

internal class CipherStreamsFactoryImpl(
    private val cipherWorker: CipherFactory
): CipherStreamsFactory {

    override fun createInputStream(base: InputStream, cipher: Cipher): SafeCloseCipherInputStream =
        SafeCloseCipherInputStream(base, cipher)

//    fun createOutput(base: OutputStream, rawKey: ByteArray) =
//       CipherOutputStream(base, cipherWorker.createFromWrappedKey(rawKey))

    override fun createOutputStream(base: OutputStream, cipher: Cipher) =
        CipherOutputStream(base, cipher)

}