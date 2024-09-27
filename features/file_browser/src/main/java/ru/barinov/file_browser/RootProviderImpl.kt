package ru.barinov.file_browser

import ru.barinov.core.FileEntity
import ru.barinov.core.Openable
import ru.barinov.core.Source
import ru.barinov.core.toInternalFileEntity
import ru.barinov.external_data.MSDRootProvider
import ru.barinov.internal_data.InternalStorageProvider

internal class RootProviderImpl(
    private val massStorageProvider: MSDRootProvider,
    private val internalStorageProvider: InternalStorageProvider
) : RootProvider {

    override fun getRootFile(type: Source): Openable? =
        when (type) {
            Source.INTERNAL
            -> getInternalRoot()
            Source.MASS_STORAGE
            -> getMassStorageRoot()
        }

    private fun getInternalRoot(): Openable? = internalStorageProvider.getInternalRoot()?.toInternalFileEntity()

    private fun getMassStorageRoot(): Openable? = massStorageProvider.msdRoot?.toInternalFileEntity()
}
