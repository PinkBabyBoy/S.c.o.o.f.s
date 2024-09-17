package ru.barinov.file_browser

import ru.barinov.core.FileEntity
import ru.barinov.core.Source
import ru.barinov.core.toFileEntity
import ru.barinov.external_data.MSDRootProvider
import ru.barinov.internal_data.InternalStorageProvider

internal class RootProviderImpl(
    private val massStorageProvider: MSDRootProvider,
    private val internalStorageProvider: InternalStorageProvider
) : RootProvider {

    override fun getRootFile(type: Source): FileEntity? =
        when (type) {
            Source.INTERNAL
            -> getInternalRoot()
            Source.MASS_STORAGE
            -> getMassStorageRoot()
        }

    private fun getInternalRoot(): FileEntity? = internalStorageProvider.getInternalRoot()?.toFileEntity()

    private fun getMassStorageRoot(): FileEntity? = massStorageProvider.msdRoot?.toFileEntity()
}
