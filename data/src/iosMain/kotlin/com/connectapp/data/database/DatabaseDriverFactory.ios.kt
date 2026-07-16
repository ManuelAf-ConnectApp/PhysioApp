package com.connectapp.data.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver

class IosDatabaseDriverFactory : DatabaseDriverFactory {
    override fun createDriver(passphrase: String): SqlDriver {
        return NativeSqliteDriver(AppDatabase.Schema, "my_app_database.db")
    }
}
