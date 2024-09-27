package ru.barinov.internal_data

import android.content.Context
import java.io.File

private const val INDEXES_FOLDER_NAME = "indexes"

internal class IndexesProviderImpl(private val appContext: Context): IndexesProvider {

    override fun provideIndexesRoot(): File =
        File(appContext.applicationInfo.dataDir, INDEXES_FOLDER_NAME).also {
            if (it.isFile) error("Something wrong in app's dir")
            if (!it.isDirectory) it.mkdir()
        }

    override fun getIndex(name: String): File =
        File(appContext.applicationInfo.dataDir, INDEXES_FOLDER_NAME + File.separator + name).also {
            if(!it.isFile) error("Not found")
        }


}
