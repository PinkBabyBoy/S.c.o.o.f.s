package ru.barinov.cryptography.factories

import ru.barinov.cryptography.KeyMemoryCache
import java.security.AlgorithmParameters
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

private const val AES_MODE = "AES/GCM/NoPadding"

internal class CipherFactoryImpl(
    private val keyMemoryCache: KeyMemoryCache
) : CipherFactory {

    private val ivLocal = byteArrayOf(123, 34, -66, -2, 34, 32, 19, -111, -5, 48, 95, -10)

    //по одному на каждую сущность
    override fun createDecryptionInnerCipher(rawSecretKey: ByteArray, iv: ByteArray?): Cipher {
        val sessionKey = keyMemoryCache.getPrivateKey()
        val envelopeCipher = Cipher.getInstance("RSA").also {
            it.init(Cipher.UNWRAP_MODE, sessionKey)
        }
        val restoredKey =
            envelopeCipher.unwrap(rawSecretKey, "AES", Cipher.SECRET_KEY) as SecretKey

        return createDecryptionInnerCipher(restoredKey, null)
    }

    override fun createDecryptionInnerCipher(key: SecretKey, iv: ByteArray?): Cipher {
        return Cipher.getInstance(AES_MODE).also {
            it.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv ?: ivLocal))
        }
    }

    override fun createEnvelopeWrapperCipher(): Cipher {
        val sessionKey = keyMemoryCache.getPrivateKey()
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

    override fun createEncryptionInnerCipher(key: SecretKey): Cipher {
        return Cipher.getInstance(AES_MODE).also {
            it.init(Cipher.ENCRYPT_MODE, key, AlgorithmParameters.getInstance("GCM"))
        }
    }
}
