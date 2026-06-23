package com.connectapp.data.datasource

import com.connectapp.domain.model.User

interface AuthLocalDataSource {

    fun clearSession()
    fun login(email: String, password: String): User?

    fun register(firstName: String, lastName: String, email: String, password: String): Result<Boolean>

    fun forgotPassword(email: String): Result<Unit>

}

