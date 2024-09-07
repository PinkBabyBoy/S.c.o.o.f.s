package ru.barinov.external_data

import kotlinx.coroutines.flow.StateFlow
import me.jahnen.libaums.core.fs.FileSystem
import me.jahnen.libaums.core.fs.UsbFile

interface MassStorageEventBus {

    val massStorageState: StateFlow<MassStorageState>
}

interface MSDFileSystemProvider {
    val fileSystem: FileSystem?
}

interface MSDRootProvider{
    val msdRoot: UsbFile?
}

sealed interface MassStorageState {

    data object Detached : MassStorageState

    class Ready(val msdFileSystem: FileSystem) : MassStorageState
}