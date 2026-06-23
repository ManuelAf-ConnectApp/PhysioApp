package com.connectapp.data.service

import com.connectapp.data.model.UserDto

interface AuthService {

    suspend fun login(email: String, password: String): UserDto

    suspend fun register(email: String, password: String): UserDto
}