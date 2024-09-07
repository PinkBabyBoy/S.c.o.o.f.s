package ru.barinov.preferences.di

import android.content.Context
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.barinov.preferences.AppPreferences
import ru.barinov.preferences.AppPreferencesImpl

private const val APP_PREFS_NAME = "SCUFS_PREFS"

val preferencesModule = module {

    single {
        AppPreferencesImpl(androidContext().getSharedPreferences(APP_PREFS_NAME, Context.MODE_PRIVATE))
    } bind AppPreferences::class
}
