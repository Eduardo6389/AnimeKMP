package com.jetbrains.kmpapp.domain.model

sealed interface AppError {
    data object SinConexion : AppError

    data object Timeout : AppError

    data class HttpCliente(
        val codigo: Int,
    ) : AppError

    data class HttpServidor(
        val codigo: Int,
    ) : AppError

    data class Parseo(
        val detalle: String,
    ) : AppError

    data class Desconocido(
        val detalle: String,
    ) : AppError
}

sealed interface Resultado<out T> {
    data class Ok<T>(
        val valor: T,
    ) : Resultado<T>

    data class Fallo(
        val error: AppError,
    ) : Resultado<Nothing>
}
