package com.jetbrains.kmpapp.di

import com.jetbrains.kmpapp.data.local.DriverFactory
import com.jetbrains.kmpapp.db.CineDb
import org.koin.dsl.module

/**
 * Arranque de Koin en iOS. Se llama desde Swift: `KoinIosKt.doInitKoinIos()` (ver iOSApp.swift).
 * El módulo de plataforma aporta el CineDb con el driver nativo de iOS (no necesita Context).
 */
fun initKoinIos() {
    initKoin(
        platformModule = module {
            single { CineDb(DriverFactory().createDriver()) }
        },
    )
}
