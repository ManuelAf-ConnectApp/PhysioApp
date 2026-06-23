package com.connectapp.domain.repository

interface NotificationPermissionRepository {
    suspend fun requestNotificationPermission(): Result<Boolean>
    suspend fun getNotificationPermission(): Result<Boolean>
}