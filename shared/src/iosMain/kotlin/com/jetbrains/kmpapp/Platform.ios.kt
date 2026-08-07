package com.jetbrains.kmpapp

import platform.UIKit.UIDevice

actual fun plataforma(): String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion

actual fun infoDispositivo(): String = UIDevice.currentDevice.model
