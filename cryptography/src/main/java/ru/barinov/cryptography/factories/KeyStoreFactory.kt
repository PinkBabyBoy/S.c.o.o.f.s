package ru.barinov.cryptography.factories

import ru.barinov.core.InteractableFile
import java.security.KeyStore

fun interface KeyStoreFactory {

    fun create(fileToStore: InteractableFile, pass: CharArray): Result<KeyStore>
}
