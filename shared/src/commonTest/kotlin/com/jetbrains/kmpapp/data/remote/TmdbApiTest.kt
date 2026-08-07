package com.jetbrains.kmpapp.data.remote

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Tests de red con MockEngine: NO tocan internet. Nosotros decidimos qué JSON "responde"
 * el servidor y comprobamos que TmdbApi lo pide bien y lo parsea bien.
 */
class TmdbApiTest {

    // Helper: crea un TmdbApi cuyo servidor SIEMPRE responde `cuerpo` con `estado`.
    // Guarda además la última URL pedida para poder verificar parámetros (api_key, page...).
    private class Servidor(
        val cuerpo: String,
        val estado: HttpStatusCode = HttpStatusCode.OK,
    ) {
        var ultimaUrl: String = ""
        val api: TmdbApi = TmdbApi(
            crearTmdbHttpClient(
                apiKey = "KEY_DE_PRUEBA",
                engine = MockEngine { request ->
                    ultimaUrl = request.url.toString()
                    respond(
                        content = cuerpo,
                        status = estado,
                        // Content-Type JSON es obligatorio para que ContentNegotiation lo parsee.
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                },
            ),
        )
    }

    private val jsonPagina = """
        {
          "page": 1,
          "results": [
            {"id": 1, "title": "Dune", "overview": "Arrakis", "vote_average": 8.4, "poster_path": "/dune.jpg", "release_date": "2021-10-22"},
            {"id": 2, "title": "Matrix", "overview": "Neo", "vote_average": 8.7, "poster_path": null, "release_date": "1999-03-31"}
          ],
          "total_pages": 5,
          "total_results": 100
        }
    """.trimIndent()

    // 1) Éxito: parsea la página y sus resultados.
    @Test
    fun populares_parsea_la_pagina() = runTest {
        val page = Servidor(jsonPagina).api.populares()
        assertEquals(1, page.page)
        assertEquals(5, page.totalPages)
        assertEquals(2, page.results.size)
        assertEquals("Dune", page.results.first().title)
    }

    // 2) La petición incluye la api_key y el número de página.
    @Test
    fun populares_manda_apikey_y_pagina() = runTest {
        val servidor = Servidor(jsonPagina)
        servidor.api.populares(pagina = 3)
        assertTrue(servidor.ultimaUrl.contains("api_key=KEY_DE_PRUEBA"), servidor.ultimaUrl)
        assertTrue(servidor.ultimaUrl.contains("page=3"), servidor.ultimaUrl)
        assertTrue(servidor.ultimaUrl.contains("movie/popular"), servidor.ultimaUrl)
    }

    // 3) JSON con lista vacía: no truena, results queda vacío.
    @Test
    fun populares_con_resultados_vacios() = runTest {
        val vacio = """{"page":1,"results":[],"total_pages":1}"""
        val page = Servidor(vacio).api.populares()
        assertTrue(page.results.isEmpty())
    }

    // 4) top_rated pega al endpoint correcto.
    @Test
    fun topRated_usa_su_endpoint() = runTest {
        val servidor = Servidor(jsonPagina)
        servidor.api.topRated()
        assertTrue(servidor.ultimaUrl.contains("movie/top_rated"), servidor.ultimaUrl)
    }

    // 5) now_playing pega al endpoint correcto.
    @Test
    fun nowPlaying_usa_su_endpoint() = runTest {
        val servidor = Servidor(jsonPagina)
        servidor.api.nowPlaying()
        assertTrue(servidor.ultimaUrl.contains("movie/now_playing"), servidor.ultimaUrl)
    }

    // 6) buscar arma ?query=... correctamente.
    @Test
    fun buscar_incluye_el_query() = runTest {
        val servidor = Servidor(jsonPagina)
        servidor.api.buscar("dune")
        assertTrue(servidor.ultimaUrl.contains("search/movie"), servidor.ultimaUrl)
        assertTrue(servidor.ultimaUrl.contains("query=dune"), servidor.ultimaUrl)
    }

    // 7) detalle parsea géneros, runtime y tagline.
    @Test
    fun detalle_parsea_generos_y_runtime() = runTest {
        val jsonDetalle = """
            {
              "id": 693134, "title": "Dune: Part Two", "overview": "...",
              "vote_average": 8.3, "poster_path": "/p.jpg", "backdrop_path": "/b.jpg",
              "release_date": "2024-02-27", "runtime": 167, "tagline": "Long live the fighters.",
              "genres": [{"id": 878, "name": "Ciencia ficción"}, {"id": 12, "name": "Aventura"}]
            }
        """.trimIndent()
        val servidor = Servidor(jsonDetalle)
        val d = servidor.api.detalle(693134)
        assertEquals(167, d.runtime)
        assertEquals(2, d.genres.size)
        assertEquals("Ciencia ficción", d.genres.first().name)
        assertTrue(servidor.ultimaUrl.contains("movie/693134"), servidor.ultimaUrl)
    }

    // 8) Error 401 (api_key inválida) → ClientRequestException.
    @Test
    fun error_401_lanza_ClientRequestException() = runTest {
        val servidor = Servidor("""{"status_message":"Invalid API key"}""", HttpStatusCode.Unauthorized)
        assertFailsWith<ClientRequestException> { servidor.api.populares() }
    }

    // 9) Error 500 (servidor caído) → ServerResponseException.
    @Test
    fun error_500_lanza_ServerResponseException() = runTest {
        val servidor = Servidor("""{"status_message":"boom"}""", HttpStatusCode.InternalServerError)
        assertFailsWith<ServerResponseException> { servidor.api.populares() }
    }

    // 10) Fallo de red (sin conexión): el motor lanza y la excepción se propaga.
    @Test
    fun fallo_de_red_se_propaga() = runTest {
        val api = TmdbApi(
            crearTmdbHttpClient(
                apiKey = "KEY",
                engine = MockEngine { throw RuntimeException("sin conexión") },
            ),
        )
        assertFailsWith<RuntimeException> { api.populares() }
    }
}
