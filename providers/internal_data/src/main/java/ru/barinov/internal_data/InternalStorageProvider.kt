package ru.barinov.internal_data

import java.io.File

fun interface InternalStorageProvider {

    fun getInternalRoot(): File?
}

