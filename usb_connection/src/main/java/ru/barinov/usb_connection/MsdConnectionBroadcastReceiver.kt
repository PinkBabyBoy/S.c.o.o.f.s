package ru.barinov.usb_connection

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbConstants.USB_CLASS_MASS_STORAGE
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import me.jahnen.libaums.core.BuildConfig
import me.jahnen.libaums.core.UsbMassStorageDevice
import me.jahnen.libaums.core.fs.FileSystem
import me.jahnen.libaums.core.fs.UsbFile
import ru.barinov.core.launchWithMutex
import ru.barinov.external_data.MSDFileSystemProvider
import ru.barinov.external_data.MSDRootProvider
import ru.barinov.external_data.MassStorageEventBus
import ru.barinov.external_data.MassStorageState
import java.io.IOException

class MsdConnectionBroadcastReceiver(
    private val appContext: Context
) : BroadcastReceiver(), MSDRootProvider, MSDFileSystemProvider, MassStorageEventBus {

    private var currentConnection: Pair<FileSystem, UsbMassStorageDevice>? = null

    private val _massStorageState =
        MutableStateFlow<MassStorageState>(MassStorageState.Detached)

    override val massStorageState = _massStorageState.asStateFlow()
    private val usbManager = appContext.getSystemService(UsbManager::class.java)

    private val coroutineScope = CoroutineScope(Job() + Dispatchers.IO)
    private val mutex = Mutex()

    init {
        ContextCompat.registerReceiver(
            appContext, this, IntentFilter().apply {
                addAction(ACTION_USB_PERMISSION)
                addAction(ACTION_USB_STATE)
                addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
                addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)

            },
            RECEIVER_NOT_EXPORTED
        )
        coroutineScope.launchWithMutex(mutex) {
            UsbMassStorageDevice.getMassStorageDevices(appContext).forEach { device ->
                if (usbManager.hasPermission(device.usbDevice)) {
                    initDevice(device.usbDevice)
                } else {
                    askPermission(device.usbDevice)
                }
            }
        }
    }

    override val msdRoot: UsbFile?
        get() = (massStorageState.value as? MassStorageState.Ready)?.msdFileSystem?.rootDirectory

    override val fileSystem: FileSystem?
        get() = (massStorageState.value as? MassStorageState.Ready)?.msdFileSystem


    override fun onReceive(context: Context?, intent: Intent?) {
        intent ?: return
        coroutineScope.launchWithMutex(mutex) {
            when (intent.action) {

                UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                    val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(UsbManager.EXTRA_DEVICE, UsbDevice::class.java)
                    } else {
                        intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                    }
                    device?.apply {
                        if (!usbManager.hasPermission(this)) {
                            askPermission(this)
                        } else {
                            initDevice(this)
                        }
                    }
                }

                UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                    close()
                }

                ACTION_USB_PERMISSION -> {
                    val device: UsbDevice? =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            intent.getParcelableExtra(
                                UsbManager.EXTRA_DEVICE,
                                UsbDevice::class.java
                            )
                        } else {
                            intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                        }
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        device?.let {
                            initDevice(it)
                        }
                    } else {

                    }
                }

                else -> {}
            }
        }
    }

    private fun hasMassStorageAttached() =
        UsbMassStorageDevice.getMassStorageDevices(appContext)
            .any { currentConnection?.second?.usbDevice?.deviceId == it.usbDevice.deviceId }

    private fun close() {
        _massStorageState.value = MassStorageState.Detached
        currentConnection?.second?.close()
        currentConnection = null
    }

    private suspend fun askPermission(device: UsbDevice) {
        if (usbManager.hasPermission(device)) {
            initDevice(device)
        } else {
            val permissionIntent =
                PendingIntent.getBroadcast(
                    appContext,
                    0,
                    Intent(ACTION_USB_PERMISSION).setPackage(appContext.packageName),
                    PendingIntent.FLAG_MUTABLE
                )
            usbManager.requestPermission(device, permissionIntent)
        }
    }

    private suspend fun initDevice(device: UsbDevice) = runCatching {
        UsbMassStorageDevice.getMassStorageDevices(appContext).find {
            it.usbDevice.deviceId == device.deviceId
        }!!.let { massStorage ->
            massStorage.init()
            massStorage.partitions.first().fileSystem to massStorage
        }
    }.fold(
        onSuccess = { initializedDevice ->
            currentConnection = initializedDevice
            _massStorageState.emit(MassStorageState.Ready(initializedDevice.first))
        },
        onFailure = {
            if (BuildConfig.DEBUG) {
                it.printStackTrace()
            }
            handleInitException(it)
        }
    )

    private fun handleInitException(it: Throwable) {
        when (it) {

        }
    }


    companion object {
        const val ACTION_USB_PERMISSION = "ru.barinov.safestore.USB_PERMISSION"
        const val ACTION_USB_STATE = "android.hardware.usb.action.USB_STATE"
        const val USB_STATE_KEY = "connect_state"
        const val USB_CONNECT_KEY = "connected"
    }

}

class MassStorageNotRecognizedException : IOException()

