package ru.barinov.cryptography.factories

import android.util.Log
import org.bouncycastle.jce.provider.BouncyCastleProvider
import ru.barinov.cryptography.KeyMemoryCache
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec


private const val AES_MODE = "AES/GCM/NoPadding"

internal class CipherFactoryImpl(
    private val keyMemoryCache: KeyMemoryCache
) : CipherFactory {

    private val ivLocal = byteArrayOf(123, 34, -66, -2, 34, 32, 19, -111, -5, 48, 95, -10)
    private val nonce = ByteArray(16).apply {
        SecureRandom().nextBytes(this)
    }

    private val BCProvider =  BouncyCastleProvider()


    //по одному на каждую сущность
    override fun createDecryptionInnerCipher(rawSecretKey: ByteArray, iv: ByteArray?): Cipher {
        val sessionKey = keyMemoryCache.getPrivateKey()
        val envelopeCipher = Cipher.getInstance("RSA").also {
            it.init(Cipher.UNWRAP_MODE, sessionKey)
        }
        val restoredKey =
            envelopeCipher.unwrap(rawSecretKey, "AES", Cipher.SECRET_KEY) as SecretKey

        return createDecryptionInnerCipherBC(restoredKey, null)
    }

    /**
     * Use for file decryption
     * */
    override fun createDecryptionInnerCipherBC(key: SecretKey, iv: ByteArray?): Cipher {
        val keySpec = SecretKeySpec(key.encoded, "AES")
        val gcmSpec = GCMParameterSpec(128, iv ?: ivLocal)
        return Cipher.getInstance(AES_MODE, BCProvider).also {
            it.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec)
        }
    }

    /**
     * Use for decryption data with keystore key
     * */
    override fun createDecryptionInnerCipher(key: SecretKey, iv: ByteArray?): Cipher {
        return Cipher.getInstance(AES_MODE).also {
            it.init(Cipher.DECRYPT_MODE,  key, GCMParameterSpec(128, iv ?: ivLocal))
        }
    }

    override fun createEnvelopeWrapperCipher(): Cipher {
        val sessionKey = keyMemoryCache.getPublicKey()
        return Cipher.getInstance("RSA").also {
            it.init(Cipher.WRAP_MODE, sessionKey)
        }
    }

    override fun createEnvelopeUnWrapperCipher(): Cipher {
        val sessionKey = keyMemoryCache.getPrivateKey()
        return Cipher.getInstance("RSA").also {
            it.init(Cipher.UNWRAP_MODE, sessionKey)
        }
    }

    /**
     * Use for encryption data with keystore key
     * */
    override fun createEncryptionInnerCipher(key: SecretKey): Cipher {
        return Cipher.getInstance(AES_MODE).also {
            it.init(Cipher.ENCRYPT_MODE, key)
        }
    }

    /**
     * Use for file encryption
     * */
    override fun createEncryptionInnerCipherBC(key: SecretKey): Cipher {
        val keySpec = SecretKeySpec(key.encoded, "AES")
        val gcmSpec = GCMParameterSpec(128, ivLocal)
        return Cipher.getInstance(AES_MODE, BCProvider).also {
            it.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec)
        }
    }
}
