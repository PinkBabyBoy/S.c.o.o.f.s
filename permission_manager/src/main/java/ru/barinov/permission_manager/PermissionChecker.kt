package ru.barinov.permission_manager

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat

class PermissionChecker(private val appContext: Context) {

    @RequiresApi(Build.VERSION_CODES.R)
    private fun hasPermissionToManageFiles(): Boolean{
       return Environment.isExternalStorageManager()
    }

    fun hasPermissionToRead(): Boolean{
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            hasPermissionToManageFiles()
        } else {
            ContextCompat.checkSelfPermission(appContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED
        }
    }

    fun hasPermissionToWriteFiles(): Boolean{
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            hasPermissionToManageFiles()
        } else {
            ContextCompat.checkSelfPermission(appContext, Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED
        }
    }
}
