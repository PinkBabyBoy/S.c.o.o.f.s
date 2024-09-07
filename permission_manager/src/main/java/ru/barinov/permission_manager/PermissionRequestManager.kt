package ru.barinov.permission_manager

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.fragment.app.Fragment
import java.io.Closeable

class PermissionRequestManager : Closeable {

    private val permissionLaunchers = mutableMapOf<Permission, PermissionData<Any>>()


    override fun close() {
        permissionLaunchers.clear()
    }


    fun launch(permission: Permission) = runCatching {
        val launcherContainer = permissionLaunchers[permission]!!
        launcherContainer.launcher.launch(launcherContainer.launcherData)
    }

    fun register(key: Permission, permissionContainer: PermissionData<*>) {
        permissionLaunchers[key] = permissionContainer as PermissionData<Any>
    }

    class PermissionLauncherBuilder(private val context: Context) {

        @Composable
        fun build(
            permission: Permission,
            resultHandler: (Boolean) -> Unit
        ): PermissionData<*> {
            return when (permission) {
                Permission.MANAGE_FILES -> {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R)
                        error("Should use ${Permission.READ_FILES} and ${Permission.WRITE_FILES} instead")

                    val uri = Uri.parse("package:${context.packageName}")
                    val intent = Intent(
                        ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                        uri
                    )
                    val launcher =
                        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                            resultHandler(Environment.isExternalStorageManager())
                        }

                    PermissionData(
                        launcher,
                        intent
                    )

                }

                Permission.NOTIFICATIONS -> {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
                        error("Current API ${Build.VERSION.SDK_INT} is to low")
                    val launcher =
                        rememberLauncherForActivityResult(
                            ActivityResultContracts.RequestPermission(),
                            resultHandler
                        )

                    PermissionData(
                        launcher,
                        Manifest.permission.POST_NOTIFICATIONS
                    )
                }

                Permission.READ_FILES -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                        error("Should use ${Permission.MANAGE_FILES} instead")
                    val launcher = rememberLauncherForActivityResult(
                        ActivityResultContracts.RequestPermission(),
                        resultHandler
                    )

                    PermissionData(
                        launcher,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                }

                Permission.WRITE_FILES -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                        error("Should use ${Permission.MANAGE_FILES} instead")
                    val launcher = rememberLauncherForActivityResult(
                        ActivityResultContracts.RequestPermission(),
                        resultHandler
                    )

                    PermissionData(
                        launcher,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                }
            }
        }
    }


}

enum class Permission {
    MANAGE_FILES, NOTIFICATIONS, READ_FILES, WRITE_FILES
}

class PermissionData<T>(
    val launcher: ActivityResultLauncher<T>,
    val launcherData: T
)