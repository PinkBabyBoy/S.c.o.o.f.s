package ru.barinov.file_browser

import ru.barinov.core.InteractableFile
import ru.barinov.core.Source
import ru.barinov.core.toInternalFileEntity
import ru.barinov.external_data.MSDRootProvider
import ru.barinov.internal_data.InternalStorageProvider

internal class RootProviderImpl(
    private val massStorageProvider: MSDRootProvider,
    private val internalStorageProvider: InternalStorageProvider
) : ru.barinov.plain_explorer.RootProvider {

    override fun getRootFile(type: Source): InteractableFile? =
        when (type) {
            Source.INTERNAL
            -> getInternalRoot()
            Source.MASS_STORAGE
            -> getMassStorageRoot()
        }

    private fun getInternalRoot(): InteractableFile? = internalStorageProvider.getInternalRoot()?.toInternalFileEntity()

    private fun getMassStorageRoot(): InteractableFile? = massStorageProvider.msdRoot?.toInternalFileEntity()
}
