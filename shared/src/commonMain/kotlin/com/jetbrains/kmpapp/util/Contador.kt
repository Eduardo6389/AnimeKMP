package com.jetbrains.kmpapp.util

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*

class Contador {
    // StateFlow: guarda un valor "vivo" que otros pueden observar
    private val _valor = MutableStateFlow(0)
    val valor: StateFlow<Int> = _valor.asStateFlow()

    fun incrementar() {
        _valor.update { it + 1 }
    }
}

// Flow: emite una cuenta regresiva, un número por segundo
fun cuentaRegresiva(desde: Int): Flow<Int> = flow {
    for (i in desde downTo 0) {
        emit(i)
        delay(1000)
    }
}
