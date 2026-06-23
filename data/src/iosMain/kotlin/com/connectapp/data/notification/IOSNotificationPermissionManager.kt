package com.connectapp.data.notification

import com.connectapp.domain.repository.NotificationPermissionRepository
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.coroutines.resume

class IOSNotificationPermissionManager : NotificationPermissionRepository {

    override suspend fun requestNotificationPermission(): Result<Boolean> = suspendCancellableCoroutine { continuation ->
        val center = UNUserNotificationCenter.currentNotificationCenter()
        val options =
            UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge

        center.requestAuthorizationWithOptions(options) { granted, _ ->
            continuation.resume(Result.success(granted))
        }
    }

    override suspend fun getNotificationPermission(): Result<Boolean> = suspendCancellableCoroutine { continuation ->
        val center = UNUserNotificationCenter.currentNotificationCenter()
        center.getNotificationSettingsWithCompletionHandler { settings ->
            val isAuthorized = settings?.authorizationStatus == UNAuthorizationStatusAuthorized
            continuation.resume(Result.success(isAuthorized))
        }
    }
}
