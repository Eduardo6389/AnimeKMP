package com.jetbrains.kmpapp.data.remote

import com.jetbrains.kmpapp.domain.model.AppError
import com.jetbrains.kmpapp.domain.model.Resultado
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.serialization.JsonConvertException
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException

suspend fun <T> llamadaSegura(block: suspend () -> T): Resultado<T> =
    try {
        Resultado.Ok(block())
    } catch (error: CancellationException) {
        throw error
    } catch (error: Throwable) {
        Resultado.Fallo(error.toAppError())
    }

private fun Throwable.toAppError(): AppError =
    when (this) {
        is ClientRequestException -> AppError.HttpCliente(response.status.value)
        is ServerResponseException -> AppError.HttpServidor(response.status.value)
        is HttpRequestTimeoutException -> AppError.Timeout
        is ConnectTimeoutException -> AppError.Timeout
        is SocketTimeoutException -> AppError.Timeout
        is JsonConvertException -> AppError.Parseo(message.orEmpty())
        is SerializationException -> AppError.Parseo(message.orEmpty())
        is IOException -> AppError.SinConexion
        else -> AppError.Desconocido(message.orEmpty())
    }
