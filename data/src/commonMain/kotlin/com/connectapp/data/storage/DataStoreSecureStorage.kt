package com.connectapp.data.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.connectapp.domain.model.User
import com.connectapp.domain.model.DomainError
import com.connectapp.domain.storage.SecureStorage
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

class DataStoreSecureStorage(
    private val dataStore: DataStore<Preferences>,
    private val cryptoManager: CryptoManager
) : SecureStorage {

    companion object {
        private const val USER_KEY = "user_data"
        private const val USERS_LIST_KEY = "users_list"
    }

    override suspend fun saveSecret(key: String, value: String) {
        val encryptedValue = cryptoManager.encrypt(value)
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey(key)] = encryptedValue
        }
    }

    override suspend fun getSecret(key: String): String? {
        val encryptedValue = dataStore.data.firstOrNull()?.get(stringPreferencesKey(key))
        return encryptedValue?.let { cryptoManager.decrypt(it) }
    }

    override suspend fun clearSecret(key: String) {
        dataStore.edit { preferences ->
            preferences.remove(stringPreferencesKey(key))
        }
    }

    override suspend fun saveUser(user: User) {
        val json = Json.encodeToString(User.serializer(), user)
        saveSecret(USER_KEY, json)
    }

    override suspend fun getCurrentUser(): User? {
        val json = getSecret(USER_KEY) ?: return null
        return try {
            Json.decodeFromString(User.serializer(), json)
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun getUser(email: String, password: String): User? {
        val users = getAllUsers()
        return users.find { it.email == email && it.password == password }
    }

    override suspend fun checkEmailExists(email: String): Boolean {
        return getAllUsers().any { it.email == email }
    }

    override suspend fun register(
        firstName: String,
        lastName: String,
        email: String,
        password: String
    ): Result<Boolean> {
        if (checkEmailExists(email)) {
            return Result.failure(DomainError.AuthError.UserAlreadyExists())
        }

        val users = getAllUsers().toMutableList()
        val newUser = User(
            id = (users.maxOfOrNull { it.id } ?: 0L) + 1,
            firstName = firstName,
            lastName = lastName,
            email = email,
            password = password
        )
        users.add(newUser)
        
        return try {
            saveAllUsers(users)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun clearUser() {
        clearSecret(USER_KEY)
    }

    private suspend fun getAllUsers(): List<User> {
        val json = getSecret(USERS_LIST_KEY) ?: return emptyList()
        return try {
            Json.decodeFromString(ListSerializer(User.serializer()), json)
        } catch (_: Exception) {
            emptyList()
        }
    }

    private suspend fun saveAllUsers(users: List<User>) {
        val json = Json.encodeToString(ListSerializer(User.serializer()), users)
        saveSecret(USERS_LIST_KEY, json)
    }
}
