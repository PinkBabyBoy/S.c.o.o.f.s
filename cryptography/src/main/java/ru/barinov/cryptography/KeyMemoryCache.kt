package ru.barinov.cryptography

import kotlinx.coroutines.flow.StateFlow
import java.io.InputStream
import java.io.OutputStream
import java.security.KeyPair
import java.security.PrivateKey
import java.security.PublicKey

interface KeyMemoryCache {

    val isLoaded: StateFlow<Boolean>

    fun initKeyStore(iStream: InputStream, pass: CharArray)

    fun getPrivateKey(): PrivateKey?

    fun getPublicKey(): PublicKey?

//    fun storePair(pair: KeyPair, pass: CharArray, readOnlyAfterWork: Boolean)

//    fun commit(oStream: OutputStream, pass: CharArray)

    fun unbind()
}