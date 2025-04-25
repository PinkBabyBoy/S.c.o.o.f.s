package ru.barinov.internal_data

import android.content.Context
import java.io.File

private const val CONTAINERS_DIR_NAME = "containers"

internal class ContainerProviderImpl(private val appContext: Context): ContainerProvider {

    override fun getContainer(fileName: String): File =
        File(appContext.applicationInfo.dataDir, "$CONTAINERS_DIR_NAME${File.separator}$fileName").also {
            if(!it.isFile) error("Not found")
        }

    override fun provideContainersRoot(): File =
        File(appContext.applicationInfo.dataDir, CONTAINERS_DIR_NAME).also {
            if(!it.isDirectory) it.mkdir()
        }


}
