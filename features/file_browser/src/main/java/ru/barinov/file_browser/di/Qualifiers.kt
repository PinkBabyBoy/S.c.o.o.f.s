package ru.barinov.file_browser.di

import org.koin.core.qualifier.Qualifier
import org.koin.core.qualifier.QualifierValue

internal object Qualifiers {

    val sharedFileTreeProvider = object : Qualifier {
        override val value: QualifierValue
            get() = "sh_file_tree_provider"

    }

    val nonSharedFileTreeProvider = object : Qualifier {
        override val value: QualifierValue
            get() = "ns_file_tree_provider"

    }

    val kpOnboardings = object : Qualifier {
        override val value: QualifierValue
            get() = "kp_onboardings"

    }

    val fbOnboardings = object : Qualifier {
        override val value: QualifierValue
            get() = "fb_onboardings"

    }
}
