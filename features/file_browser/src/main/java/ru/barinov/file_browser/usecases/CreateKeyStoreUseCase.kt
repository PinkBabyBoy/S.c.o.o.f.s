package ru.barinov.file_browser.usecases

import ru.barinov.core.FileEntity
import ru.barinov.core.Openable
import ru.barinov.core.inputStream
import ru.barinov.core.toInternalFileEntity
import ru.barinov.cryptography.KeyMemoryCache
import ru.barinov.cryptography.factories.KeyStoreFactory
import java.io.File

class CreateKeyStoreUseCase(
    private val keyStoreFactory: KeyStoreFactory,
    private val keyMemoryCache: KeyMemoryCache
) {

    operator fun invoke(
        folder: Openable,
        password: CharArray,
        name: String,
        loadInstantly: Boolean
    ): Result<Unit> = runCatching {
        val file = folder.let {
            when (it) {
                is FileEntity.InternalFile -> {
                    File(it.attachedOrigin, name).apply {
                        if (!isFile) createNewFile() else error("")
                    }.toInternalFileEntity()
                }

                is FileEntity.MassStorageFile -> {
                    if(it.attachedOrigin.search(name) != null) error("")
                    it.attachedOrigin.createFile(name).toInternalFileEntity()
                }
            }
        }
        keyStoreFactory.create(file, password).onSuccess {
            if (loadInstantly) keyMemoryCache.initKeyStore(file.inputStream(), password)
        }.onFailure { throw it }
    }
}
