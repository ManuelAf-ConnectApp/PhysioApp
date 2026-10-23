package com.connectapp.physioapp.database

import com.connectapp.core.crypto.secureRandomBytes
import com.connectapp.core.crypto.toHex

/** Minimal secret storage used by [DatabasePassphraseProvider]; backed by KVault in the app. */
internal interface SecretStore {
    fun read(key: String): String?
    fun write(key: String, value: String): Boolean
}

/**
 * Supplies the SQLCipher passphrase of PhysioApp's local database (docs/00-auditoria-y-plan.md,
 * D7): 32 bytes from the platform CSPRNG, hex-encoded, generated on first use and kept in the
 * OS keystore (Android Keystore / iOS Keychain, device-bound).
 *
 * It never regenerates an existing passphrase: if the stored value were lost, the database could
 * no longer be opened and the app would report it instead of silently starting an empty one.
 */
internal class DatabasePassphraseProvider(
    private val store: SecretStore,
    private val randomBytes: (Int) -> ByteArray = ::secureRandomBytes,
) {
    fun getOrCreate(): String {
        store.read(KEY)?.let { return it }
        val passphrase = toHex(randomBytes(PASSPHRASE_BYTES))
        check(store.write(KEY, passphrase)) { "Could not store the database key in the OS keystore" }
        return checkNotNull(store.read(KEY)) { "The database key could not be read back from the OS keystore" }
    }

    internal companion object {
        const val KEY = "physio_database_passphrase"
        const val PASSPHRASE_BYTES = 32
    }
}
