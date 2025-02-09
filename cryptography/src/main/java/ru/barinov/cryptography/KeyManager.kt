package ru.barinov.cryptography

import ru.barinov.core.InteractableFile


interface KeyManager: KeyStateProvider {

    fun loadKey(
        keyFile: InteractableFile,
        password: CharArray,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    )

    fun unbindKey()
}
