package ru.barinov.component_initializer

import android.content.Context
import androidx.startup.Initializer
import org.koin.core.context.GlobalContext.loadKoinModules
import ru.barinov.external_data.di.msdProviderModule

class MsdProviderInitializer: Initializer<Unit> {

    override fun create(context: Context) {
        loadKoinModules(msdProviderModule)
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> = mutableListOf()
}
