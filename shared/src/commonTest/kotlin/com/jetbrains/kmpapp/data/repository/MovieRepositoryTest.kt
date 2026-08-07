package com.jetbrains.kmpapp.data.repository

import com.jetbrains.kmpapp.data.local.MovieLocalDataSource
import com.jetbrains.kmpapp.data.remote.TmdbApi
import com.jetbrains.kmpapp.data.remote.crearTmdbHttpClient
import com.jetbrains.kmpapp.domain.model.Movie
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests del repositorio OFFLINE-FIRST.
 * Usamos un [MovieLocalDataSource] "de mentira" en memoria (sin SQLite real) y un [TmdbApi]
 * con MockEngine (sin internet). Así probamos la LÓGICA de combinar red + DB local.
 */
class MovieRepositoryTest {

    /** DB falsa en memoria que imita el comportamiento real (incluye preservar favoritos). */
    private class FakeLocal : MovieLocalDataSource {
        private val estado = MutableStateFlow<List<Movie>>(emptyList())
        override fun observarTodas(): Flow<List<Movie>> = estado
        override fun observarFavoritas(): Flow<List<Movie>> = estado.map { l -> l.filter { it.esFavorita } }
        override fun observarPorId(id: Int): Flow<Movie?> = estado.map { l -> l.find { it.id == id } }
        override suspend fun guardarTodas(movies: List<Movie>) {
            val favoritas = estado.value.filter { it.esFavorita }.map { it.id }.toSet()
            estado.value = movies.map { it.copy(esFavorita = it.id in favoritas) }
        }
        override suspend fun marcarFavorita(id: Int, favorita: Boolean) {
            estado.value = estado.value.map { if (it.id == id) it.copy(esFavorita = favorita) else it }
        }
    }

    private val jsonPagina = """
        {"page":1,"results":[
          {"id":1,"title":"Dune","overview":"a","vote_average":8.4,"poster_path":"/d.jpg","release_date":"2021-01-01"},
          {"id":2,"title":"Matrix","overview":"b","vote_average":8.7,"poster_path":null,"release_date":"1999-01-01"}
        ],"total_pages":1}
    """.trimIndent()

    private fun apiQueResponde(cuerpo: String) = TmdbApi(
        crearTmdbHttpClient(
            apiKey = "KEY",
            engine = MockEngine {
                respond(cuerpo, headers = headersOf(HttpHeaders.ContentType, "application/json"))
            },
        ),
    )

    @Test
    fun refrescar_baja_de_red_y_guarda_en_local() = runTest {
        val local = FakeLocal()
        val repo = MovieRepositoryImpl(apiQueResponde(jsonPagina), local)

        assertTrue(repo.observarPopulares().first().isEmpty()) // arranca vacío
        repo.refrescarPopulares()
        val guardadas = repo.observarPopulares().first()
        assertEquals(2, guardadas.size)
        assertEquals("Dune", guardadas.first().titulo)
    }

    @Test
    fun observar_lee_de_local_sin_tocar_la_red() = runTest {
        val local = FakeLocal()
        // Precargamos la DB (como si ya hubiera datos de una sesión previa, sin internet ahora).
        local.guardarTodas(listOf(Movie(9, "Cacheada", "", 7.0, null, "2020")))
        // Un API que fallaría si se usara (motor que lanza): demostramos que observar NO lo llama.
        val repo = MovieRepositoryImpl(
            TmdbApi(crearTmdbHttpClient("KEY", MockEngine { throw RuntimeException("no debe llamarse") })),
            local,
        )
        val lista = repo.observarPopulares().first()
        assertEquals(1, lista.size)
        assertEquals("Cacheada", lista.first().titulo)
    }

    @Test
    fun marcar_favorita_actualiza_favoritas() = runTest {
        val local = FakeLocal()
        val repo = MovieRepositoryImpl(apiQueResponde(jsonPagina), local)
        repo.refrescarPopulares()

        assertTrue(repo.observarFavoritas().first().isEmpty())
        repo.marcarFavorita(1, true)
        val favoritas = repo.observarFavoritas().first()
        assertEquals(1, favoritas.size)
        assertEquals(1, favoritas.first().id)
        assertTrue(favoritas.first().esFavorita)
    }

    @Test
    fun refrescar_preserva_el_favorito_existente() = runTest {
        val local = FakeLocal()
        val repo = MovieRepositoryImpl(apiQueResponde(jsonPagina), local)
        repo.refrescarPopulares()
        repo.marcarFavorita(1, true)      // marcamos Dune

        repo.refrescarPopulares()          // segundo refresco: NO debe borrar el favorito
        val dune = repo.observarPelicula(1).first()
        assertTrue(dune!!.esFavorita, "El favorito debe sobrevivir al refresco de la red")
    }

    @Test
    fun buscar_devuelve_resultados_de_red() = runTest {
        val repo = MovieRepositoryImpl(apiQueResponde(jsonPagina), FakeLocal())
        val resultados = repo.buscar("dune")
        assertEquals(2, resultados.size)
    }

    @Test
    fun observar_pelicula_por_id() = runTest {
        val local = FakeLocal()
        val repo = MovieRepositoryImpl(apiQueResponde(jsonPagina), local)
        repo.refrescarPopulares()
        assertEquals("Matrix", repo.observarPelicula(2).first()?.titulo)
    }
}
