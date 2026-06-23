package com.connectapp.data.repository

import app.cash.sqldelight.coroutines.asFlow
import com.connectapp.data.database.AppDatabase
import com.connectapp.data.mapper.UserMapper
import com.connectapp.domain.model.User
import com.connectapp.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList

class UserRepositoryImpl(
    database: AppDatabase,
    private val userMapper: UserMapper

) : UserRepository {
    private val queries = database.appDatabaseQueries

    override fun observeUser(id: String): Flow<User?> {
        return queries.getUserById(id.toLong()).asFlow().map { userEntity ->
            userEntity.let { userMapper.toDomain(it.executeAsOne()) }
        }
    }

    override suspend fun saveUser(user: User) {
        queries.insertUser(
            firstName = user.firstName,
            lastName = user.lastName,
            email = user.email,
            password = user.password
        )
    }

    override suspend fun getAllUsers(): List<User> {
        return queries.selectAll().asFlow().map { userEntity ->
            userMapper.toDomain(userEntity.executeAsOne())
        }.toList()
    }
}