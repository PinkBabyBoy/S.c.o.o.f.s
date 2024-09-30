package ru.barinov.file_browser

import ru.barinov.core.Addable
import ru.barinov.core.Source
import ru.barinov.core.toInternalFileEntity
import ru.barinov.external_data.MSDRootProvider
import ru.barinov.internal_data.InternalStorageProvider

internal class RootProviderImpl(
    private val massStorageProvider: MSDRootProvider,
    private val internalStorageProvider: InternalStorageProvider
) : RootProvider {

    override fun getRootFile(type: Source): Addable? =
        when (type) {
            Source.INTERNAL
            -> getInternalRoot()
            Source.MASS_STORAGE
            -> getMassStorageRoot()
        }

    private fun getInternalRoot(): Addable? = internalStorageProvider.getInternalRoot()?.toInternalFileEntity()

    private fun getMassStorageRoot(): Addable? = massStorageProvider.msdRoot?.toInternalFileEntity()
}
