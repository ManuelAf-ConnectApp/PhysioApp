package com.connectapp.data.storage

interface CryptoManager {
    fun encrypt(value: String): String
    fun decrypt(value: String): String
}