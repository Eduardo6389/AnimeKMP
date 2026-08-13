package com.jetbrains.kmpapp.platform

import kotlinx.coroutines.flow.StateFlow

interface Conectividad {
    val conectado: StateFlow<Boolean>
}

expect fun crearConectividad(contexto: Any?): Conectividad
