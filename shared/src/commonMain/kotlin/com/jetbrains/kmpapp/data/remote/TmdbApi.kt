package com.jetbrains.kmpapp.data.remote

import com.jetbrains.kmpapp.config.CineBuildConfig
import com.jetbrains.kmpapp.data.dto.MovieDetailDto
import com.jetbrains.kmpapp.data.dto.MoviePageDto
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

/**
 * Construye un HttpClient de Ktor ya configurado para TMDB:
 *   - ContentNegotiation + JSON → convierte el JSON en nuestros DTOs automáticamente.
 *   - defaultRequest → pone base URL, api_key e idioma en TODAS las peticiones (no repetir).
 *
 * El parámetro `engine` es la clave para TESTEAR: en producción va null (Ktor usa el motor
 * real de cada plataforma: OkHttp en Android, Darwin en iOS); en los tests le pasamos un
 * MockEngine que responde JSON inventado, sin tocar internet.
 */
fun crearTmdbHttpClient(
    apiKey: String = CineBuildConfig.TMDB_API_KEY,
    engine: HttpClientEngine? = null,
): HttpClient {
    val config: HttpClientConfig<*>.() -> Unit = {
        // expectSuccess = true → una respuesta 4xx/5xx lanza excepción (ClientRequestException /
        // ServerResponseException) en vez de intentar parsear el cuerpo de error como película.
        expectSuccess = true
        install(ContentNegotiation) {
            // ignoreUnknownKeys = true → si TMDB añade campos nuevos, no truena el parseo.
            json(Json { ignoreUnknownKeys = true })
        }
        defaultRequest {
            // Dentro de defaultRequest el receptor NO es HttpRequestBuilder, así que los query
            // params se agregan sobre el URLBuilder (url.parameters) en vez de con parameter().
            url(CineBuildConfig.TMDB_BASE_URL)              // "https://api.themoviedb.org/3/"
            url.parameters.append("api_key", apiKey)        // autenticación en cada request
            url.parameters.append("language", "es-MX")      // títulos y sinopsis en español
        }
    }
    // Con motor explícito (tests) o con el motor por defecto de la plataforma (producción).
    return if (engine != null) HttpClient(engine, config) else HttpClient(config)
}

/**
 * Cliente de la API de TMDB. Recibe un HttpClient ya configurado (inyección de dependencias):
 * así esta clase NO sabe de api keys ni de motores, solo de "qué endpoint pedir".
 *
 * Cada función es `suspend`: se puede pausar mientras baja datos sin congelar la UI.
 * `.body()` deserializa la respuesta JSON al tipo de retorno (MoviePageDto / MovieDetailDto).
 */
class TmdbApi(private val client: HttpClient) {

    /** Películas populares (paginado). Endpoint: /movie/popular */
    suspend fun populares(pagina: Int = 1): MoviePageDto =
        client.get("movie/popular") { parameter("page", pagina) }.body()

    /** Mejor valoradas. Endpoint: /movie/top_rated */
    suspend fun topRated(pagina: Int = 1): MoviePageDto =
        client.get("movie/top_rated") { parameter("page", pagina) }.body()

    /** En cartelera. Endpoint: /movie/now_playing */
    suspend fun nowPlaying(pagina: Int = 1): MoviePageDto =
        client.get("movie/now_playing") { parameter("page", pagina) }.body()

    /** Detalle de una película por id. Endpoint: /movie/{id} */
    suspend fun detalle(id: Int): MovieDetailDto =
        client.get("movie/$id").body()

    /** Búsqueda por texto. Endpoint: /search/movie?query=... */
    suspend fun buscar(query: String, pagina: Int = 1): MoviePageDto =
        client.get("search/movie") {
            parameter("query", query)
            parameter("page", pagina)
        }.body()
}
