package ru.barinov.cryptography.factories

import android.os.Build
import androidx.annotation.RequiresApi
import ru.barinov.core.FileEntity
import ru.barinov.core.outputStream
import ru.barinov.cryptography.keygens.AsymmetricKeyGenerator
import java.security.KeyStore


//TODO to build config
const val PubKeyAllias = "PubKEY"
const val PrKeyAllias = "PrKEY"

internal class KeyStoreFactoryImpl(
    private val keygen: AsymmetricKeyGenerator
) : KeyStoreFactory {

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun create(fileToStore: FileEntity, pass: CharArray): Result<KeyStore> = runCatching {
        KeyStore.getInstance(KeyStore.getDefaultType()).apply {
            load(null)
            keygen.generateNewKeyPair().apply {
                setKeyEntry(PubKeyAllias, public, pass, null)
                setKeyEntry(PrKeyAllias, private, pass, arrayOf(SelfSignedCert().cert()))
            }
            store(fileToStore.outputStream(), pass)
        }
    }
}
