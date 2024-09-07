package ru.barinov.cryptography.keygens

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.SecureRandom
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

private const val SECRET_KEY_SIZE = 256
private const val PAIR_KEY_SIZE = 4096

internal class SecretKeyGeneratorImpl: SecretKeyGenerator {

    private val generator = KeyGenerator.getInstance("AES").also {
        it.init(SECRET_KEY_SIZE, SecureRandom())
    }

    override fun generateNewSecretKey(): SecretKey =
        generator.generateKey()

    override fun generateSyncKeyWithKeyStore(alias: String): SecretKey {
        val spec = KeyGenParameterSpec.Builder(alias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(SECRET_KEY_SIZE)
            .build()
       return KeyGenerator.getInstance("AES", "AndroidKeyStore").also { it.init(spec) }.generateKey()
    }
}

class AsymmetricKeyGeneratorImpl: AsymmetricKeyGenerator {

    private val generator = KeyPairGenerator.getInstance("RSA").also {
        it.initialize(PAIR_KEY_SIZE)
    }

    override fun generateNewKeyPair(): KeyPair =
        generator.generateKeyPair()

}
