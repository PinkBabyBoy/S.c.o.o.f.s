package ru.barinov.internal_data

import java.io.File

interface ContainerProvider {

    fun getContainer(fileName: String): File

    fun provideContainersRoot(): File
}
