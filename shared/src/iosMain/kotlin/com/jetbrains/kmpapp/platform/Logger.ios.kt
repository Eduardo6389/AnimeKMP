package com.jetbrains.kmpapp.platform

import platform.Foundation.NSLog

actual object Logger {
    actual fun d(mensaje: String) {
        NSLog("%@", mensaje)
    }
}
