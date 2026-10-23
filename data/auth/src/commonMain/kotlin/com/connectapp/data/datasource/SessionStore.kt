package com.connectapp.data.datasource

import com.liftric.kvault.KVault

/**
 * Persists a remembered session (account id + last interaction time) so it can be restored after
 * an app restart. Only used when the user ticks "remember session"; otherwise the session lives
 * in memory and ends with the process.
 */
interface SessionStore {
    fun accountId(): String?
    fun lastActivityAt(): Long?
    fun save(accountId: String, lastActivityAt: Long)
    fun saveLastActivity(lastActivityAt: Long)
    fun clear()
}

/**
 * [SessionStore] in the OS secure storage (Android Keystore-backed preferences / iOS Keychain,
 * device-only). Only the account id is stored — never credentials.
 */
class KVaultSessionStore(private val kVault: KVault) : SessionStore {
    override fun accountId(): String? = kVault.string(KEY_ACCOUNT_ID)

    override fun lastActivityAt(): Long? = kVault.string(KEY_LAST_ACTIVITY)?.toLongOrNull()

    override fun save(accountId: String, lastActivityAt: Long) {
        kVault.set(KEY_ACCOUNT_ID, accountId)
        saveLastActivity(lastActivityAt)
    }

    override fun saveLastActivity(lastActivityAt: Long) {
        kVault.set(KEY_LAST_ACTIVITY, lastActivityAt.toString())
    }

    // Also removes the template's former "auth_token" entry from installs that still have it.
    override fun clear() {
        kVault.clear()
    }

    private companion object {
        const val KEY_ACCOUNT_ID = "session_account_id"
        const val KEY_LAST_ACTIVITY = "session_last_activity"
    }
}
