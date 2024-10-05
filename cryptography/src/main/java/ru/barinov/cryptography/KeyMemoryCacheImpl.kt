package ru.barinov.cryptography

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.barinov.cryptography.factories.PrKeyAllias
import ru.barinov.cryptography.factories.PubKeyAllias
import java.io.InputStream
import java.security.KeyPair
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey

internal class KeyMemoryCacheImpl: KeyMemoryCache {

    private var keyStore = KeyStore.getInstance(KeyStore.getDefaultType())

    private var pass: CharArray? = null

    private val _isLoaded: MutableStateFlow<Boolean> = MutableStateFlow(false)
    override val isLoaded = _isLoaded.asStateFlow()


    override fun initKeyStore(iStream: InputStream, pass: CharArray) {
        iStream.use {
            keyStore.load(it, pass)
            //To check password
            keyStore.getKey(PubKeyAllias, pass)
            this.pass = pass
            _isLoaded.value = true
        }
    }

    override fun getPrivateKey(): PrivateKey? =
        keyStore.getKey(PrKeyAllias, pass) as? PrivateKey

    override fun getPublicKey(): PublicKey? =
        keyStore.getKey(PubKeyAllias, pass) as? PublicKey

//    /**
//     * @param readOnlyAfterWork means what container encrypted with key will be 1 session write use only
//     */
//    override fun storePair(pair: KeyPair, pass: CharArray, readOnlyAfterWork: Boolean){
//        storePublicKey(pair.public, pass)
//        if(readOnlyAfterWork){
//            synchronized(cache){
//                cache.privateKeyCache = pair.private
//            }
//        } else {
//            storePrivateKey(pair.private, pass)
//        }
//    }

//    private fun storePublicKey(key: PublicKey, pass: CharArray){
//       keyStore.setKeyEntry("pubK", key, pass, null)
//    }
//
//    private fun storePrivateKey(key: PrivateKey, pass: CharArray){
//        keyStore.setKeyEntry("privK", key, pass, null)
//    }

//    override fun commit(oStream: OutputStream, pass: CharArray){
//        keyStore.store(oStream, pass)
//    }

    override fun unbind() {
        pass = null
        keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
        _isLoaded.value = false
    }
}
