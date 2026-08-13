package com.jetbrains.kmpapp.di

import android.content.Context
import com.jetbrains.kmpapp.data.local.DriverFactory
import com.jetbrains.kmpapp.db.AnimeDb
import com.jetbrains.kmpapp.platform.Conectividad
import com.jetbrains.kmpapp.platform.crearConectividad
import org.koin.dsl.module

fun initKoinAndroid(context: Context) {
    initKoin(
        platformModule =
            module {
                single { AnimeDb(DriverFactory(context).createDriver()) }
                single<Conectividad> { crearConectividad(context) }
            },
    )
}
