package ru.barinov.file_browser.di

import org.koin.core.qualifier.Qualifier
import org.koin.core.qualifier.QualifierValue

internal object Qualifiers {

    val kpOnboardings = object : Qualifier {
        override val value: QualifierValue
            get() = "kp_onboardings"

    }

    val fbOnboardings = object : Qualifier {
        override val value: QualifierValue
            get() = "fb_onboardings"

    }

    val fileEntityMapper = object : Qualifier {
        override val value: QualifierValue
            get() = "fileEntityMapper"

    }

    val fileIndexMapper = object : Qualifier {
        override val value: QualifierValue
            get() = "fileIndex"

    }

    val singleFileBus = object : Qualifier {
        override val value: QualifierValue
            get() = "singleFileBus"

    }

    val bulkFileBus = object : Qualifier {
        override val value: QualifierValue
            get() = "bulkFileBus"

    }
}
