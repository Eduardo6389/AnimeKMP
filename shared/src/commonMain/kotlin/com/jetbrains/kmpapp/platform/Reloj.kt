package com.jetbrains.kmpapp.platform

import kotlin.time.Clock

object Reloj {
    fun ahoraMillis(): Long = Clock.System.now().toEpochMilliseconds()
}
