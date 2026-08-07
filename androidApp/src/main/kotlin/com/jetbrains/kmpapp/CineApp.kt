package com.jetbrains.kmpapp

import android.app.Application
import com.jetbrains.kmpapp.di.initKoinAndroid

/**
 * Clase Application de Android: es lo primero que arranca. Aquí iniciamos Koin pasándole el
 * `Context` de la app (lo necesita el driver de SQLite). Registrada en AndroidManifest.xml.
 */
class CineApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoinAndroid(this)
    }
}
