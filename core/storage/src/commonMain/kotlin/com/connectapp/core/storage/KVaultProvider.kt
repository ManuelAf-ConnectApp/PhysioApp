package com.connectapp.core.storage

import com.liftric.kvault.KVault

/**
 * Creates the platform-backed [KVault] instance for secure key-value storage.
 *
 * [name] namespaces the underlying store — the Android Keystore-backed file name, or the iOS
 * Keychain service name — so callers should pass a value that's stable across app launches, and
 * distinct per logical store if more than one is ever needed.
 */
expect fun createKVault(name: String): KVault

/**
 * Like [createKVault], but entries never leave this device: on iOS they use
 * `kSecAttrAccessibleWhenUnlockedThisDeviceOnly`, so they are neither synced nor restored to
 * another device from a backup. On Android, KVault's EncryptedSharedPreferences are already
 * excluded from cloud backup and device transfer by the app's data_extraction_rules.xml.
 *
 * Use it for secrets that only make sense on the device that created them, such as the local
 * database key.
 */
expect fun createDeviceBoundKVault(name: String): KVault
