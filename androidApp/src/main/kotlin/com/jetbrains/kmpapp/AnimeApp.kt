package com.jetbrains.kmpapp

import android.app.Application
import com.jetbrains.kmpapp.di.initKoinAndroid

class AnimeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoinAndroid(this)
    }
}