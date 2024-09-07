package ru.barinov.component_initializer

import android.content.Context
import androidx.startup.Initializer
import org.koin.core.context.GlobalContext.loadKoinModules
import ru.barinov.protected_enter.di.protectedEnterModule

class ProtectedEnterInitializer: Initializer<Unit> {

    override fun create(context: Context) {
        loadKoinModules(protectedEnterModule)
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> = mutableListOf()
}
