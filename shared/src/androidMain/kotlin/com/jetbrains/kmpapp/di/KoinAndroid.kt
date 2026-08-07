package com.jetbrains.kmpapp.di

import android.content.Context
import com.jetbrains.kmpapp.data.local.DriverFactory
import com.jetbrains.kmpapp.db.CineDb
import org.koin.dsl.module

/**
 * Arranque de Koin en ANDROID. Se llama desde CineApp.onCreate(this).
 * El módulo de plataforma aporta el CineDb creado con el driver de Android (necesita Context).
 */
fun initKoinAndroid(context: Context) {
    initKoin(
        platformModule = module {
            single { CineDb(DriverFactory(context).createDriver()) }
        },
    )
}