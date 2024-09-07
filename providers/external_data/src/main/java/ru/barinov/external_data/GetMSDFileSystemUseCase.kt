package ru.barinov.external_data

import me.jahnen.libaums.core.fs.FileSystem

class GetMSDFileSystemUseCase(
    private val fileSystemProvider: MSDFileSystemProvider
) {

    operator fun invoke(): FileSystem? = fileSystemProvider.fileSystem
}