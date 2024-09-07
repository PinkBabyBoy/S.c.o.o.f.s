package ru.barinov.permission_manager.di

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import ru.barinov.permission_manager.PermissionChecker

val permissionManagerModule = module {

    factory {
        PermissionChecker(androidContext())
    }

}
