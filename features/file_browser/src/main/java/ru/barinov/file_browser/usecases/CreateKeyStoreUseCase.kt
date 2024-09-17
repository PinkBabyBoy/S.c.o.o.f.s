package ru.barinov.file_browser.usecases

import android.util.Log
import ru.barinov.core.FileEntity
import ru.barinov.core.inputStream
import ru.barinov.core.toFileEntity
import ru.barinov.cryptography.KeyMemoryCache
import ru.barinov.cryptography.factories.KeyStoreFactory
import java.io.File

class CreateKeyStoreUseCase(
    private val keyStoreFactory: KeyStoreFactory,
    private val keyMemoryCache: KeyMemoryCache
) {

    operator fun invoke(
        folder: FileEntity,
        password: CharArray,
        name: String,
        loadInstantly: Boolean
    ): Result<Unit> = runCatching {
        val file = folder.let {
            when (it) {
                is FileEntity.InternalFile -> {
                    File(it.attachedOrigin, name).apply {
                        if (!isFile) createNewFile() else error("")
                    }.toFileEntity()
                }

                is FileEntity.MassStorageFile -> {
                    if(it.attachedOrigin.search(name) != null) error("")
                    it.attachedOrigin.createFile(name).toFileEntity()
                }
            }
        }
        keyStoreFactory.create(file, password).onSuccess {
            if (loadInstantly) keyMemoryCache.initKeyStore(file.inputStream(), password)
        }.onFailure { throw it }
    }
}
