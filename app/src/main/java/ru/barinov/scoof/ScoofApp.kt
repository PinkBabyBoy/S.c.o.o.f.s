package ru.barinov.scoof

import android.annotation.SuppressLint
import android.app.Application
import android.content.IntentFilter
import android.hardware.usb.UsbManager
import android.os.Build
import androidx.startup.AppInitializer
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.dsl.koinApplication
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
import ru.barinov.usb_connection.MsdConnectionBroadcastReceiver.Companion.ACTION_USB_PERMISSION
import ru.barinov.usb_connection.MsdConnectionBroadcastReceiver.Companion.ACTION_USB_STATE

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
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            registerReceiver(
//                MsdConnectionBroadcastReceiver(this),
//                IntentFilter().apply {
//                    addAction(ACTION_USB_PERMISSION)
//                    addAction(ACTION_USB_STATE)
//                    addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
//                    addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED)
//                }, RECEIVER_NOT_EXPORTED
//            )
//        } else {
//            registerReceiver(
//                MsdConnectionBroadcastReceiver(this),
//                IntentFilter().apply {
//                    addAction(ACTION_USB_PERMISSION)
//                    addAction(ACTION_USB_STATE)
//                    addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
//                    addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED)
//                }
//            )
//        }
    }
}