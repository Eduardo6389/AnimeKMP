package com.jetbrains.kmpapp.data.local

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.jetbrains.kmpapp.db.CineDb

/**
 * Crea el driver de SQLite para ANDROID. Necesita un [Context] (lo exige el sistema Android
 * para localizar/crear el archivo de base de datos).
 *
 * Nota de diseño: NO usamos expect/actual aquí. El `expect class` obligaría a un constructor
 * común, pero Android requiere Context y iOS no requiere nada. Por eso hay dos clases
 * `DriverFactory` (una por plataforma) con el mismo método `createDriver()`. La versión que se
 * use la decide el módulo de Koin de cada plataforma (Sesión 5).
 */
class DriverFactory(private val context: Context) {
    fun createDriver(): SqlDriver =
        AndroidSqliteDriver(
            schema = CineDb.Schema,
            context = context,
            name = "cine.db", // nombre del archivo físico de la base de datos
        )
}
