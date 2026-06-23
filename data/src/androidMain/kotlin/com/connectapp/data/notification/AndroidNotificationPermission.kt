package com.connectapp.data.notification

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.connectapp.domain.repository.NotificationPermissionRepository

class AndroidNotificationPermissionManager(
    private val activity: ComponentActivity
) : NotificationPermissionRepository, LifecycleEventObserver {

    private var onGrantedCallback: (() -> Unit)? = null
    private var onDeniedCallback: (() -> Unit)? = null

    private val requestPermissionLauncher = activity.activityResultRegistry.register(
        "notification_permission_launcher",
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            onGrantedCallback?.invoke()
        } else {
            onDeniedCallback?.invoke()
        }
    }

    init {
        activity.lifecycle.addObserver(this)
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_DESTROY) {
            requestPermissionLauncher.unregister()
            source.lifecycle.removeObserver(this)
        }
    }

    override suspend fun requestNotificationPermission(): Result<Boolean> {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                   return Result.success(true)
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            // En versiones anteriores a Android 13 (API 33), el permiso se acepta al instalar la app
            return Result.success(true)
        }

        return Result.success(false)
    }

    override suspend fun getNotificationPermission(): Result<Boolean> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val isGranted = ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            return Result.success(isGranted)
        } else {
            return Result.success(true)
        }
    }

}