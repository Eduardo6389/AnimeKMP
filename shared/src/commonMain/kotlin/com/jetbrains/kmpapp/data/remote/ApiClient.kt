package com.jetbrains.kmpapp.data.remote

import com.jetbrains.kmpapp.data.remote.dto.AnimePageDto
import com.jetbrains.kmpapp.data.remote.dto.AnimeResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.url
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json

fun crearHttpClient(engine: HttpClientEngine? = null): HttpClient {
    val config: HttpClientConfig<*>.() -> Unit = {
        expectSuccess = true
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        install(HttpRequestRetry) {
            maxRetries = MAX_REINTENTOS
            retryIf { _, response ->
                response.status.value == TOO_MANY_REQUESTS ||
                    response.status.value in 500..599
            }
            exponentialDelay()
        }
        install(HttpTimeout) {
            requestTimeoutMillis = TIMEOUT_MILLIS
        }
        defaultRequest {
            url(BASE_URL)
        }
    }
    return if (engine == null) HttpClient(config) else HttpClient(engine, config)
}

class ApiClient(
    private val client: HttpClient,
    private val intervaloMillis: Long = INTERVALO_MILLIS,
) {
    private val mutex = Mutex()

    suspend fun listado(pagina: Int): AnimePageDto =
        limitar {
            client
                .get("top/anime") {
                    parameter("page", pagina)
                }.body()
        }

    suspend fun detalle(id: String): AnimeResponseDto =
        limitar {
            client.get("anime/$id/full").body()
        }

    suspend fun buscar(
        query: String,
        pagina: Int = 1,
    ): AnimePageDto =
        limitar {
            client
                .get("anime") {
                    parameter("q", query)
                    parameter("page", pagina)
                }.body()
        }

    private suspend fun <T> limitar(block: suspend () -> T): T =
        mutex.withLock {
            delay(intervaloMillis)
            block()
        }
}

private const val BASE_URL = "https://api.jikan.moe/v4/"
private const val TIMEOUT_MILLIS = 10_000L
private const val INTERVALO_MILLIS = 1_100L
private const val MAX_REINTENTOS = 2
private const val TOO_MANY_REQUESTS = 429
