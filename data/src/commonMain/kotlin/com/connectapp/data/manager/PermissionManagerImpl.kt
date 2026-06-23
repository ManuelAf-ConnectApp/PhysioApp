package com.connectapp.data.manager

import com.connectapp.domain.manager.PermissionManager
import com.connectapp.domain.repository.NotificationPermissionRepository

class PermissionManagerImpl(
    private val notificationPermissionRepository: NotificationPermissionRepository
): PermissionManager {
    override suspend fun requestNotificationPermission(): Result<Boolean> {
        return notificationPermissionRepository.requestNotificationPermission()
    }

    override suspend fun getNotificationPermission(): Result<Boolean> {
        return notificationPermissionRepository.getNotificationPermission()
    }
}