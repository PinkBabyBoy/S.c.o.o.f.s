package ru.barinov.component_initializer

import android.content.Context
import androidx.startup.Initializer
import org.koin.core.context.GlobalContext.loadKoinModules
import ru.barinov.password_manager.di.passwordManagerModule
import ru.barinov.preferences.di.preferencesModule

class PreferencesInitializer: Initializer<Unit> {

    override fun create(context: Context) {
        loadKoinModules(preferencesModule)
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> = mutableListOf()
}
