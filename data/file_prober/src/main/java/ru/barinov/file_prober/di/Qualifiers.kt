package ru.barinov.file_prober.di

import org.koin.core.qualifier.Qualifier
import org.koin.core.qualifier.QualifierValue

object Qualifiers {

    val encryptedFileInfoExtractor = object : Qualifier {
        override val value: QualifierValue
            get() = "encryptedFileInfoExtractor"

    }

    val plaintFileInfoExtractor = object : Qualifier {
        override val value: QualifierValue
            get() = "plaintFileInfoExtractor"

    }
}
