package ru.barinov.cryptography.factories

import ru.barinov.core.FileEntity
import java.security.KeyStore

fun interface KeyStoreFactory {

    fun create(fileToStore: FileEntity, pass: CharArray): Result<KeyStore>
}
