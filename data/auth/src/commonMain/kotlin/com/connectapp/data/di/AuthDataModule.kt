package com.connectapp.data.di

import com.connectapp.core.crypto.Pbkdf2PasswordHasher
import com.connectapp.core.database.currentEpochMillis
import com.connectapp.core.storage.createDeviceBoundKVault
import com.connectapp.data.datasource.KVaultSessionStore
import com.connectapp.data.datasource.SessionStore
import com.connectapp.data.repository.AuthRepositoryImpl
import com.connectapp.domain.policy.EpochClock
import com.connectapp.domain.repository.AuthRepository
import org.koin.dsl.module

// Stable across launches: renaming it would orphan a remembered session (see KVaultProvider.kt).
// Not renamed to "physioapp_*" on purpose: the template name here is the organization prefix
// (connectapp), confirmed to stay (docs/00-auditoria-y-plan.md, D10).
private const val AUTH_SESSION_STORE_NAME = "connectapp_auth_session"

/**
 * :data:auth owns its own Koin module. No HTTP client: PhysioApp is offline-first. The
 * [com.connectapp.core.database.physio.PhysioDatabase] comes from composeApp's databaseModule.
 */
val dataModule = module {
    single { EpochClock { currentEpochMillis() } }
    single<SessionStore> { KVaultSessionStore(createDeviceBoundKVault(name = AUTH_SESSION_STORE_NAME)) }
    single<AuthRepository> {
        AuthRepositoryImpl(
            database = get(),
            sessionStore = get(),
            hasher = Pbkdf2PasswordHasher(),
            clock = get(),
        )
    }
}
