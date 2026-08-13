package com.jetbrains.kmpapp.platform

import android.util.Log

actual object Logger {
    actual fun d(mensaje: String) {
        Log.d("AnimeKMP", mensaje)
    }
}
