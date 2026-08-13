package com.jetbrains.kmpapp.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.jetbrains.kmpapp.db.AnimeDb

actual class DriverFactory actual constructor(
    contexto: Any?,
) {
    actual fun createDriver(): SqlDriver =
        JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).also {
            AnimeDb.Schema.create(it)
        }
}
