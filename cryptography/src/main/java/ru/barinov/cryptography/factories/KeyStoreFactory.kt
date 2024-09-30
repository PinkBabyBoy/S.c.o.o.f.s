package ru.barinov.cryptography.factories

import ru.barinov.core.Addable
import java.security.KeyStore

fun interface KeyStoreFactory {

    fun create(fileToStore: Addable, pass: CharArray): Result<KeyStore>
}
