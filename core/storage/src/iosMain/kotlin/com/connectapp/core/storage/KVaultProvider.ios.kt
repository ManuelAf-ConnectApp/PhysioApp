package com.connectapp.core.storage

import com.liftric.kvault.KVault

actual fun createKVault(name: String): KVault = KVault(serviceName = name)

actual fun createDeviceBoundKVault(name: String): KVault =
    KVault(serviceName = name, accessibility = KVault.Accessible.WhenUnlockedThisDeviceOnly)
