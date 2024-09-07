package ru.barinov.protected_enter.di

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.barinov.core.NavPartDeployer
import ru.barinov.protected_enter.EnterScreenViewModel

val protectedEnterModule = module {

    viewModel {
        EnterScreenViewModel(
            hashCreator = get(),
            hashValidator = get(),
            passwordStorage = get(),
            cleaner = get(),
            permissionChecker = get(),
        )
    }
}
