package com.jetbrains.kmpapp.data.remote

import com.jetbrains.kmpapp.data.remote.dto.AnimePageDto
import com.jetbrains.kmpapp.data.remote.dto.AnimeResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.url
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun crearJikanHttpClient(
    engine: HttpClientEngine? = null,
): HttpClient {
    val config: HttpClientConfig<*>.() -> Unit = {
        expectSuccess = true

        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }

        defaultRequest {
            url("https://api.jikan.moe/v4/")
        }
    }

    return if (engine != null) {
        HttpClient(engine, config)
    } else {
        HttpClient(config)
    }
}

class JikanApi(
    private val client: HttpClient,
) {
    suspend fun listado(pagina: Int = 1): AnimePageDto =
        client.get("top/anime") {
            parameter("page", pagina)
        }.body()

    suspend fun detalle(id: Int): AnimeResponseDto =
        client.get("anime/$id/full").body()

    suspend fun buscar(query: String, pagina: Int = 1): AnimePageDto =
        client.get("anime") {
            parameter("q", query)
            parameter("page", pagina)
        }.body()
}
