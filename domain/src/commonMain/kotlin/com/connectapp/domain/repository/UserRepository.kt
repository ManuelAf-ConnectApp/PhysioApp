package com.connectapp.domain.repository

import com.connectapp.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {

    fun observeUser(id: String): Flow<User?>

     suspend fun saveUser(user: User)

     suspend fun getAllUsers(): List<User>
}