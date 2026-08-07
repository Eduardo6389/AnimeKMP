package com.jetbrains.kmpapp.util

import platform.Foundation.NSDate
import platform.Foundation.NSUUID
import platform.Foundation.timeIntervalSince1970

actual fun nuevoUuid(): String = NSUUID().UUIDString()

actual fun epochMillis(): Long = (NSDate().timeIntervalSince1970 * 1000).toLong()

actual class Reloj {
    actual fun ahora(): Long = (NSDate().timeIntervalSince1970 * 1000).toLong()
}
