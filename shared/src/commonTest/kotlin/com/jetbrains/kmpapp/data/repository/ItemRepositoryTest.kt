package com.jetbrains.kmpapp.data.repository

import com.jetbrains.kmpapp.data.remote.ApiClient
import com.jetbrains.kmpapp.data.remote.crearHttpClient
import com.jetbrains.kmpapp.domain.model.AppError
import com.jetbrains.kmpapp.domain.model.Resultado
import com.jetbrains.kmpapp.test.FakeConectividad
import com.jetbrains.kmpapp.test.FakeLocalDataSource
import com.jetbrains.kmpapp.test.item
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ItemRepositoryTest {
    @Test
    fun paginaRemotaSeGuardaLocalmente() =
        runTest {
            val local = FakeLocalDataSource()
            val repo = repo(local = local)
            repo.cargarPagina(1)
            assertEquals(
                "Naruto",
                local.catalogo.value
                    .first()
                    .titulo,
            )
        }

    @Test
    fun paginaUnoReemplazaCache() =
        runTest {
            val local = FakeLocalDataSource()
            repo(local = local).cargarPagina(1)
            assertTrue(local.reemplazo)
        }

    @Test
    fun paginaDevuelveSiHayMas() =
        runTest {
            val resultado = repo().cargarPagina(1) as Resultado.Ok
            assertTrue(resultado.valor)
        }

    @Test
    fun sinRedBusquedaUsaCache() =
        runTest {
            val local =
                FakeLocalDataSource().apply {
                    ultimaBusqueda = listOf(item(titulo = "Naruto"))
                }
            val resultado = repo(local, conectado = false).buscar("Naruto") as Resultado.Ok
            assertEquals("Naruto", resultado.valor.first().titulo)
        }

    @Test
    fun sinRedSinCacheDevuelveSinConexion() =
        runTest {
            val resultado = repo(conectado = false).buscar("Nada")
            assertIs<AppError.SinConexion>((resultado as Resultado.Fallo).error)
        }

    @Test
    fun detalleUsaCacheCuandoNoHayRed() =
        runTest {
            val local =
                FakeLocalDataSource().apply {
                    detalle =
                        com.jetbrains.kmpapp.domain.model.ItemDetalle(
                            item(),
                            "Descripcion",
                            emptyList(),
                            emptyList(),
                        )
                }
            val resultado = repo(local, conectado = false).obtenerDetalle("1")
            assertEquals("Descripcion", (resultado as Resultado.Ok).valor.descripcion)
        }

    @Test
    fun ttlVigenteNoEstaExpirado() =
        runTest {
            val local =
                FakeLocalDataSource().apply {
                    timestamp =
                        com.jetbrains.kmpapp.platform.Reloj
                            .ahoraMillis()
                }
            assertFalse(repo(local).cacheExpirada(60_000L))
        }

    private fun repo(
        local: FakeLocalDataSource = FakeLocalDataSource(),
        conectado: Boolean = true,
    ): ItemRepositoryImpl {
        val engine =
            MockEngine {
                respond(
                    JSON,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }
        return ItemRepositoryImpl(
            ApiClient(crearHttpClient(engine)),
            local,
            FakeConectividad(conectado),
        )
    }

    companion object {
        private const val JSON =
            """{"data":[{"mal_id":20,"title":"Naruto"}],"pagination":{"has_next_page":true}}"""
    }
}
