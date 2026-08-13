package com.jetbrains.kmpapp.data.remote

import com.jetbrains.kmpapp.domain.model.AppError
import com.jetbrains.kmpapp.domain.model.Resultado
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ApiClientTest {
    @Test
    fun listado200() =
        runTest {
            val api = apiCon(JSON_LISTA)
            val respuesta = api.listado(1)
            assertEquals("Naruto", respuesta.datos.first().titulo)
        }

    @Test
    fun listadoVacio() =
        runTest {
            val api = apiCon(JSON_VACIO)
            assertTrue(api.listado(1).datos.isEmpty())
        }

    @Test
    fun paginacionIndicaSiguiente() =
        runTest {
            val api = apiCon(JSON_LISTA)
            assertTrue(api.listado(1).paginacion.haySiguiente)
        }

    @Test
    fun buscarEnviaQuery() =
        runTest {
            var url = ""
            val engine =
                MockEngine { request ->
                    url = request.url.toString()
                    respuesta(JSON_LISTA)
                }
            ApiClient(crearHttpClient(engine)).buscar("Naruto")
            assertTrue(url.contains("q=Naruto"))
        }

    @Test
    fun error401EsHttpCliente() =
        runTest {
            val resultado =
                llamadaSegura {
                    apiCon("{}", HttpStatusCode.Unauthorized).listado(1)
                }
            assertIs<AppError.HttpCliente>((resultado as Resultado.Fallo).error)
        }

    @Test
    fun error404ConservaCodigo() =
        runTest {
            val resultado =
                llamadaSegura {
                    apiCon("{}", HttpStatusCode.NotFound).detalle("1")
                }
            val error = (resultado as Resultado.Fallo).error as AppError.HttpCliente
            assertEquals(404, error.codigo)
        }

    @Test
    fun error500EsHttpServidor() =
        runTest {
            val resultado =
                llamadaSegura {
                    apiCon("{}", HttpStatusCode.InternalServerError).listado(1)
                }
            assertIs<AppError.HttpServidor>((resultado as Resultado.Fallo).error)
        }

    @Test
    fun jsonMalformadoEsParseo() =
        runTest {
            val resultado =
                llamadaSegura {
                    apiCon("{mal json").listado(1)
                }
            assertIs<AppError.Parseo>((resultado as Resultado.Fallo).error)
        }

    @Test
    fun timeoutEsTipado() =
        runTest {
            val engine =
                MockEngine { request ->
                    throw HttpRequestTimeoutException(request.url.toString(), 1000)
                }
            val resultado =
                llamadaSegura {
                    ApiClient(crearHttpClient(engine)).listado(1)
                }
            assertIs<AppError.Timeout>((resultado as Resultado.Fallo).error)
        }

    private fun apiCon(
        json: String,
        status: HttpStatusCode = HttpStatusCode.OK,
    ): ApiClient {
        val engine =
            MockEngine {
                respuesta(json, status)
            }
        return ApiClient(crearHttpClient(engine))
    }

    private fun MockRequestHandleScope.respuesta(
        json: String,
        status: HttpStatusCode = HttpStatusCode.OK,
    ) = respond(
        content = json,
        status = status,
        headers = headersOf(HttpHeaders.ContentType, "application/json"),
    )

    companion object {
        private const val JSON_LISTA =
            """{"data":[{"mal_id":20,"title":"Naruto"}],"pagination":{"has_next_page":true}}"""
        private const val JSON_VACIO =
            """{"data":[],"pagination":{"has_next_page":false}}"""
    }
}
