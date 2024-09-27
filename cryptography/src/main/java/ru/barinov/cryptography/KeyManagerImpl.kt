package ru.barinov.cryptography

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.barinov.core.Openable
import ru.barinov.core.inputStream
import ru.barinov.core.launchCatching


internal class KeyManagerImpl(
    private val keyCache: KeyMemoryCache
): KeyManager {

    private val mutex = Mutex()

    private val serviceCoroutine =
        CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val isKeyLoaded: StateFlow<Boolean> = keyCache.isLoaded

    override fun loadKey(
        keyFile: Openable,
        password: CharArray,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        serviceCoroutine.launchCatching(
            block = {
                mutex.withLock {
                    val input = keyFile.inputStream()
                    keyCache.initKeyStore(input, password)
                }
            },
            onSuccess = {
                onSuccess()
            },
            onError = onError
        )
    }

    override fun unbindKey() {
        keyCache.unbind()
    }
}
