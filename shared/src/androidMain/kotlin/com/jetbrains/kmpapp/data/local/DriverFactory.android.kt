package com.jetbrains.kmpapp.data.local

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.jetbrains.kmpapp.db.AnimeDb

actual class DriverFactory actual constructor(
    private val contexto: Any?,
) {
    actual fun createDriver(): SqlDriver {
        val context = requireNotNull(contexto as? Context)
        return AndroidSqliteDriver(
            schema = AnimeDb.Schema,
            context = context,
            name = NOMBRE_DB,
        )
    }
}

private const val NOMBRE_DB = "anime.db"
