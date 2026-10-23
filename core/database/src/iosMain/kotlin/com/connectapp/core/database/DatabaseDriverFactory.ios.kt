package com.connectapp.core.database

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.native.NativeSqliteDriver

actual fun createSqlDriver(schema: SqlSchema<QueryResult.Value<Unit>>, databaseName: String): SqlDriver =
    NativeSqliteDriver(schema = schema, name = databaseName)
