package ru.barinov.component_initializer

import android.content.Context
import androidx.startup.Initializer
import org.koin.core.context.GlobalContext.loadKoinModules
import ru.barinov.password_manager.di.passwordManagerModule

class PasswordManagerInitializer: Initializer<Unit> {

    override fun create(context: Context) {
        loadKoinModules(passwordManagerModule)
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> = mutableListOf()
}
