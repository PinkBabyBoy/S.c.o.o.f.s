package ru.barinov.scoof

import android.annotation.SuppressLint
import android.app.Application

import androidx.startup.AppInitializer
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import ru.barinov.component_initializer.CryptographyInitializer
import ru.barinov.component_initializer.EnterScreenInitializer
import ru.barinov.component_initializer.FileObserverInitializer
import ru.barinov.component_initializer.FileWorksInitializer
import ru.barinov.component_initializer.InternalStorageInitializer
import ru.barinov.component_initializer.MsdProviderInitializer
import ru.barinov.component_initializer.PasswordManagerInitializer
import ru.barinov.component_initializer.PermissionManagerInitializer
import ru.barinov.component_initializer.PreferencesInitializer
import ru.barinov.component_initializer.ProtectedEnterInitializer
import ru.barinov.component_initializer.TransactionManagerInitializer
import ru.barinov.component_initializer.UsbConnectionInitializer
import ru.barinov.usb_connection.MsdConnectionBroadcastReceiver

class ScoofApp: Application() {

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@ScoofApp)
        }
        AppInitializer.getInstance(this).apply {
            initializeComponent(ProtectedEnterInitializer::class.java)
            initializeComponent(PasswordManagerInitializer::class.java)
            initializeComponent(PreferencesInitializer::class.java)
            initializeComponent(MsdProviderInitializer::class.java)
            initializeComponent(CryptographyInitializer::class.java)
            initializeComponent(PermissionManagerInitializer::class.java)
            initializeComponent(UsbConnectionInitializer::class.java)
            initializeComponent(EnterScreenInitializer::class.java)
            initializeComponent(FileWorksInitializer::class.java)
            initializeComponent(InternalStorageInitializer::class.java)
            initializeComponent(TransactionManagerInitializer::class.java)
            initializeComponent(FileObserverInitializer::class.java)
        }
        MsdConnectionBroadcastReceiver(this)
    }
}