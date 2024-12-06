package ru.barinov.protected_enter.di

import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import ru.barinov.protected_enter.EnterScreenViewModel
import ru.barinov.protected_enter.fingerprint.BiometricEnterHelper

val protectedEnterModule = module {

    viewModel {
        EnterScreenViewModel(
            hashCreator = get(),
            hashValidator = get(),
            passwordStorage = get(),
            cleaner = get(),
            permissionChecker = get(),
            biometricEnterHelper = get()
        )
    }

    factory { BiometricEnterHelper(androidContext()) }
}
