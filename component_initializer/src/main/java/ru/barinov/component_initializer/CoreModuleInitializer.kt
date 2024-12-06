package ru.barinov.component_initializer

import android.content.Context
import androidx.startup.Initializer
import org.koin.core.context.GlobalContext.loadKoinModules
import ru.barinov.core.di.coreModule

class CoreModuleInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        loadKoinModules(coreModule)
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> = mutableListOf()
}