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
}
