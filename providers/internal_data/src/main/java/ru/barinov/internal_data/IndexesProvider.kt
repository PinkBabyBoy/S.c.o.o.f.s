package ru.barinov.internal_data

import java.io.File

interface IndexesProvider {

    fun provideIndexesRoot(): File

    fun getIndex(name: String): File
}
