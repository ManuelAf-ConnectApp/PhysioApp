package com.connectapp.data.database

import app.cash.sqldelight.db.SqlDriver

interface DatabaseDriverFactory {
    fun createDriver(passphrase: String): SqlDriver
}
