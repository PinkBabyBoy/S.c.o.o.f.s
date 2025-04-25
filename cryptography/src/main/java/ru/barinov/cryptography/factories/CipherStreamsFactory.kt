package ru.barinov.cryptography.factories

import java.io.InputStream
import java.io.OutputStream
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream

interface CipherStreamsFactory {

    fun createInputStream(base: InputStream, cipher: Cipher): SafeCloseCipherInputStream

//    fun createOutput(base: OutputStream, rawKey: ByteArray) =
//       CipherOutputStream(base, cipherWorker.createFromWrappedKey(rawKey))

    fun createOutputStream(base: OutputStream, cipher: Cipher): CipherOutputStream
}

class SafeCloseCipherInputStream(base: InputStream, cipher: Cipher): CipherInputStream(base, cipher){

    override fun close() {
       runCatching {  super.close() }
    }
}