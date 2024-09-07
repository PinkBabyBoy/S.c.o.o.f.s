package ru.barinov.component_initializer

import android.content.Context
import androidx.startup.Initializer
import org.koin.core.context.GlobalContext.loadKoinModules
import ru.barinov.file_works.di.fileworkModule

class FileWorksInitializer: Initializer<Unit> {

    override fun create(context: Context) {
        loadKoinModules(fileworkModule)
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> = mutableListOf()
}
