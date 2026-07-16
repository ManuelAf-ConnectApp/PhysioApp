package com.connectapp.data.database

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import net.sqlcipher.database.SupportFactory

class AndroidDatabaseDriverFactory(private val context: Context) : DatabaseDriverFactory {
    override fun createDriver(passphrase: String): SqlDriver {
        val factory = SupportFactory(passphrase.toByteArray())
        return AndroidSqliteDriver(
            schema = AppDatabase.Schema,
            context = context,
            name = "my_app_database.db",
            factory = factory
        )
    }
}
