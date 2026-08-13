package com.jetbrains.kmpapp.platform

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

actual fun crearConectividad(contexto: Any?): Conectividad =
    object : Conectividad {
        override val conectado: StateFlow<Boolean> = MutableStateFlow(true)
    }
