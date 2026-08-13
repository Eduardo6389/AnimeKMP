package com.jetbrains.kmpapp.data.local

import app.cash.sqldelight.db.SqlDriver

expect class DriverFactory(
    contexto: Any?,
) {
    fun createDriver(): SqlDriver
}
