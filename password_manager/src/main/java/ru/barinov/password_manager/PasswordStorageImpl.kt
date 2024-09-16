package ru.barinov.password_manager

import ru.barinov.cryptography.factories.CipherFactory
import ru.barinov.cryptography.keygens.SecretKeyGenerator
import ru.barinov.preferences.AppPreferences
import java.security.KeyStore
import java.util.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

private const val KEYSTORE_ALIAS = "pass_key"

internal class PasswordStorageImpl(
    private val secretKeyGen: SecretKeyGenerator,
    private val cipherFactory: CipherFactory,
    private val storage: AppPreferences
) : PasswordStorage {

    private val keyStore = KeyStore.getInstance("AndroidKeyStore").also {
        it.load(null)
    }

    @Synchronized
    override fun store(hash: ByteArray, type: PType) {
        if (!keyStore.containsAlias(KEYSTORE_ALIAS)) {
            keyStore.setKeyEntry(
                KEYSTORE_ALIAS, secretKeyGen.generateSyncKeyWithKeyStore(
                    KEYSTORE_ALIAS
                ), null, null
            )
        }
        val key =
            (keyStore.getEntry(KEYSTORE_ALIAS, null) as KeyStore.SecretKeyEntry).secretKey
        val cipher = cipherFactory.createEncryptionInnerCipher(key)
        val passHash = Base64.getEncoder().encode(
            cipher.doFinal(hash)
        ).decodeToString()
        storage.iv = Base64.getEncoder().encode(cipher.iv).decodeToString()
        when (type) {
            PType.REAL -> storage.tPass = passHash
            PType.EMERGENCY -> storage.fPass = passHash
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    @Synchronized
    override fun readHash(pType: PType): ByteArray? {
        if (!keyStore.containsAlias(KEYSTORE_ALIAS)) error("")
        val key =
            (keyStore.getEntry(KEYSTORE_ALIAS, null) as KeyStore.SecretKeyEntry).secretKey
        val encryptedHash = (if (pType == PType.REAL) storage.tPass else storage.fPass)?.let {
            Base64.getDecoder().decode(it)
        } ?: return null

        return cipherFactory.createDecryptionInnerCipher(
            key,
            Base64.getDecoder().decode(storage.iv.orEmpty())
        ).doFinal(encryptedHash)
    }

    @Synchronized
    override fun hasPasswordSet(): Boolean = keyStore.containsAlias(KEYSTORE_ALIAS) && !storage.tPass.isNullOrEmpty()

    override fun clear() {
        runCatching {
            keyStore.deleteEntry(KEYSTORE_ALIAS)
            storage.tPass = null
            storage.fPass = null
        }

    }
}

enum class PType {
    REAL, EMERGENCY
}

interface PasswordStorage {

    fun store(hash: ByteArray, type: PType)

    fun readHash(pType: PType): ByteArray?

    fun hasPasswordSet(): Boolean

    fun clear()
}
