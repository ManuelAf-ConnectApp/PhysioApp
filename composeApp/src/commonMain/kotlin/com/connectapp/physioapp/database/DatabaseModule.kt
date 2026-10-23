package com.connectapp.physioapp.database

import com.connectapp.core.database.EncryptedPhysioDatabase
import com.connectapp.core.database.openPhysioDatabase
import com.connectapp.core.database.physio.PhysioDatabase
import com.connectapp.core.storage.createDeviceBoundKVault
import com.liftric.kvault.KVault
import org.koin.dsl.module

// Stable across releases: renaming it would orphan the key and make the database unreadable.
private const val DATABASE_KEY_STORE_NAME = "connectapp_database_key"

private class KVaultSecretStore(private val kVault: KVault) : SecretStore {
    override fun read(key: String): String? = kVault.string(key)
    override fun write(key: String, value: String): Boolean = kVault.set(key, value)
}

/**
 * Single encrypted [PhysioDatabase] shared by every `:data:*` module. Koin singletons are lazy:
 * the database is opened (and created on first run) the first time a repository needs it.
 */
val databaseModule = module {
    single<EncryptedPhysioDatabase> {
        val passphrase = DatabasePassphraseProvider(
            KVaultSecretStore(createDeviceBoundKVault(DATABASE_KEY_STORE_NAME))
        ).getOrCreate()
        openPhysioDatabase(passphrase)
    }
    single<PhysioDatabase> { get<EncryptedPhysioDatabase>().database }
}
