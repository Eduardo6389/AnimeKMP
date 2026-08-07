package com.jetbrains.kmpapp.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.jetbrains.kmpapp.db.CineDb

/**
 * Crea el driver de SQLite para iOS (Kotlin/Native). No necesita Context: el sistema
 * gestiona la ruta del archivo. Misma superficie que la versión Android: `createDriver()`.
 */
class DriverFactory {
    fun createDriver(): SqlDriver =
        NativeSqliteDriver(
            schema = CineDb.Schema,
            name = "cine.db",
        )
}
