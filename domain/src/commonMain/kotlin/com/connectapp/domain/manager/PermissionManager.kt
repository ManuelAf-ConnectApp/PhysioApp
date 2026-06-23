package com.connectapp.domain.manager

interface PermissionManager {

    suspend fun requestNotificationPermission(): Result<Boolean>

    suspend fun getNotificationPermission(): Result<Boolean>
}
