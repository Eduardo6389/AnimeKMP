package com.jetbrains.kmpapp.util

expect fun nuevoUuid(): String

expect fun epochMillis(): Long

expect class Reloj() {
    fun ahora(): Long
}
