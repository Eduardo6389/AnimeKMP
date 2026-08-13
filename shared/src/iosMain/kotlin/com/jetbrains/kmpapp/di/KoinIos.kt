package com.jetbrains.kmpapp.di

import com.jetbrains.kmpapp.data.local.DriverFactory
import com.jetbrains.kmpapp.db.AnimeDb
import com.jetbrains.kmpapp.platform.Conectividad
import com.jetbrains.kmpapp.platform.crearConectividad
import org.koin.dsl.module

fun initKoinIos() {
    initKoin(
        platformModule =
            module {
                single { AnimeDb(DriverFactory(null).createDriver()) }
                single<Conectividad> { crearConectividad(null) }
            },
    )
}
