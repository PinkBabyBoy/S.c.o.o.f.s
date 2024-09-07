package ru.barinov.component_initializer

import android.content.Context
import androidx.startup.Initializer
import org.koin.core.context.GlobalContext.loadKoinModules
import ru.barinov.internal_data.di.internalStorageProviderModule

class InternalStorageInitializer: Initializer<Unit> {

    override fun create(context: Context) {
        loadKoinModules(internalStorageProviderModule)
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> = mutableListOf()
}
