package com.connectapp.physioapp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform