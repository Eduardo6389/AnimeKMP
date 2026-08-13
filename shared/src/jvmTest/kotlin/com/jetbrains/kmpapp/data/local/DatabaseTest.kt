package com.jetbrains.kmpapp.data.local

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.jetbrains.kmpapp.db.AnimeDb
import com.jetbrains.kmpapp.domain.model.Orden
import com.jetbrains.kmpapp.test.item
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DatabaseTest {
    private lateinit var driver: JdbcSqliteDriver
    private lateinit var local: SqlDelightItemLocalDataSource

    @BeforeTest
    fun preparar() {
        driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        AnimeDb.Schema.create(driver)
        local =
            SqlDelightItemLocalDataSource(
                AnimeDb(driver),
                Dispatchers.Unconfined,
            )
    }

    @AfterTest
    fun cerrar() {
        driver.close()
    }

    @Test
    fun insertaItem() =
        runTest {
            local.guardar(listOf(item()), 1L)
            assertEquals(1, local.observarCatalogo(Orden.METRICA_DESC).first().size)
        }

    @Test
    fun reemplazaDatosDelMismoId() =
        runTest {
            local.guardar(listOf(item(titulo = "Viejo")), 1L)
            local.guardar(listOf(item(titulo = "Nuevo")), 2L)
            val actual = local.observarCatalogo(Orden.METRICA_DESC).first().first()
            assertEquals("Nuevo", actual.titulo)
        }

    @Test
    fun favoritoSeGuarda() =
        runTest {
            local.guardar(listOf(item()), 1L)
            local.alternarFavorito("1")
            assertEquals(1, local.observarFavoritos().first().size)
        }

    @Test
    fun flowReflejaCambios() =
        runTest {
            local.guardar(listOf(item(id = "1")), 1L)
            local.guardar(listOf(item(id = "2")), 1L)
            assertEquals(2, local.observarCatalogo(Orden.METRICA_DESC).first().size)
        }

    @Test
    fun limpiarCacheConservaFavorito() =
        runTest {
            local.guardar(listOf(item(id = "1"), item(id = "2")), 1L)
            local.alternarFavorito("1")
            local.limpiarCache()
            val actual = local.observarCatalogo(Orden.METRICA_DESC).first()
            assertEquals(listOf("1"), actual.map { it.id })
        }

    @Test
    fun ordenaPorTituloEnBaseDeDatos() =
        runTest {
            local.guardar(
                listOf(
                    item(id = "1", titulo = "Zeta"),
                    item(id = "2", titulo = "Alpha"),
                ),
                1L,
            )
            val actual = local.observarCatalogo(Orden.TITULO_ASC).first()
            assertEquals(listOf("Alpha", "Zeta"), actual.map { it.titulo })
        }

    @Test
    fun busquedaLocalFiltraTitulo() =
        runTest {
            local.guardar(listOf(item(titulo = "Naruto"), item(id = "2", titulo = "Bleach")), 1L)
            assertTrue(local.buscar("Naru").all { it.titulo == "Naruto" })
        }
}
