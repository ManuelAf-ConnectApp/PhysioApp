package com.connectapp.domain.usecase

import com.connectapp.domain.manager.PermissionManager

class GetNotificationPermissionUseCase(
    private val permissionManager: PermissionManager
) {
     suspend operator fun invoke(): Result<Boolean> {
        return permissionManager.getNotificationPermission()
    }
}