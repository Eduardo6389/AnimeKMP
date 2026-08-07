package com.jetbrains.kmpapp.presentation

import com.jetbrains.kmpapp.domain.model.Movie
import com.jetbrains.kmpapp.domain.model.MovieDetail
import com.jetbrains.kmpapp.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * Repositorio "de mentira" para probar los ViewModels SIN red ni base de datos reales.
 * Guardamos las películas en memoria y exponemos "palancas" para simular escenarios
 * (que la red falle, qué devuelve la búsqueda, qué hay "en el servidor" al refrescar).
 */
class FakeMovieRepository(
    peliculasEnDb: List<Movie> = emptyList(),
) : MovieRepository {

    private val db = MutableStateFlow(peliculasEnDb)

    /** "Datos que hay en el servidor" y que aparecen al llamar a refrescarPopulares(). */
    var enServidor: List<Movie> = emptyList()
    var fallarAlRefrescar: Boolean = false
    var resultadosBusqueda: List<Movie> = emptyList()
    var detalleADevolver: MovieDetail? = null

    override fun observarPopulares(): Flow<List<Movie>> = db
    override fun observarFavoritas(): Flow<List<Movie>> = db.map { l -> l.filter { it.esFavorita } }
    override fun observarPelicula(id: Int): Flow<Movie?> = db.map { l -> l.find { it.id == id } }

    override suspend fun refrescarPopulares() {
        if (fallarAlRefrescar) throw RuntimeException("401 unauthorized (simulado)")
        val favoritas = db.value.filter { it.esFavorita }.map { it.id }.toSet()
        db.value = enServidor.map { it.copy(esFavorita = it.id in favoritas) }
    }

    override suspend fun buscar(query: String): List<Movie> = resultadosBusqueda

    override suspend fun obtenerDetalle(id: Int): MovieDetail =
        detalleADevolver ?: MovieDetail(
            id = id, titulo = "", overview = "", rating = 0.0, posterUrl = null,
            backdropUrl = null, anio = "", duracionMin = null, generos = emptyList(), tagline = null,
        )

    override suspend fun marcarFavorita(id: Int, favorita: Boolean) {
        db.value = db.value.map { if (it.id == id) it.copy(esFavorita = favorita) else it }
    }
}

/** Fábrica corta para tests. */
fun peli(id: Int, titulo: String = "Peli $id", favorita: Boolean = false) =
    Movie(id = id, titulo = titulo, overview = "", rating = id.toDouble(), posterUrl = null, anio = "2024", esFavorita = favorita)
