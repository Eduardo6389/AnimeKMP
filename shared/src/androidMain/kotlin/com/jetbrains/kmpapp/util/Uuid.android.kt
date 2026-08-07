package com.jetbrains.kmpapp.util

actual fun nuevoUuid(): String = java.util.UUID.randomUUID().toString()

actual fun epochMillis(): Long = System.currentTimeMillis()

actual class Reloj {
    actual fun ahora(): Long = System.currentTimeMillis()
}
