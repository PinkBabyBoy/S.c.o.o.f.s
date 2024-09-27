package ru.barinov.cryptography.factories

import ru.barinov.core.FileEntity
import ru.barinov.core.Openable
import java.security.KeyStore

fun interface KeyStoreFactory {

    fun create(fileToStore: Openable, pass: CharArray): Result<KeyStore>
}
