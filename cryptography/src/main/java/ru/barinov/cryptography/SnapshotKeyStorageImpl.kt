package ru.barinov.cryptography

import android.util.Log
import ru.barinov.core.getBytes
import ru.barinov.cryptography.factories.CipherFactory
import ru.barinov.cryptography.keygens.SecretKeyGenerator
import java.security.KeyStore

private const val KEY_ALIAS = "hash_key"

internal class SnapshotKeyStorageImpl(
    private val secretKeyGen: SecretKeyGenerator,
    private val cipherFactory: CipherFactory
) : SnapshotKeyStorage {

    private val keyStore = KeyStore.getInstance("AndroidKeyStore").also {
        it.load(null)
        if (!it.containsAlias(KEY_ALIAS)) {
            it.setKeyEntry(
                KEY_ALIAS, secretKeyGen.generateSyncKeyWithKeyStore(
                    KEY_ALIAS
                ), null, null //TODO password
            )
        }
    }


    override fun encrypt(hash: ByteArray): ByteArray {
        val key =
            (keyStore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry).secretKey
        val cipher = cipherFactory.createEncryptionInnerCipher(key)
        val encHash = cipher.doFinal(hash)
        return  (cipher.iv.size.getBytes() + cipher.iv + encHash.size.getBytes() + encHash)
    }

    override fun decrypt(encryptedHash: ByteArray, iv: ByteArray): ByteArray {
        val key =
            (keyStore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry).secretKey
        return cipherFactory.createDecryptionInnerCipher(key, iv).doFinal(encryptedHash) //TODO IV?
    }
}