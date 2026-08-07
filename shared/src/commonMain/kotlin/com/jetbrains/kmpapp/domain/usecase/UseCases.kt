package com.jetbrains.kmpapp.domain.usecase

import com.jetbrains.kmpapp.domain.model.Movie
import com.jetbrains.kmpapp.domain.model.MovieDetail
import com.jetbrains.kmpapp.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow

/**
 * CASOS DE USO (capa domain): cada uno representa UNA acción de negocio.
 *
 * ¿Por qué existen si "solo llaman al repositorio"? Porque:
 *  - Dan nombres claros a las intenciones ("obtener populares", "marcar favorita").
 *  - El ViewModel depende de acciones pequeñas, no del repositorio entero.
 *  - Aquí iría lógica extra si hiciera falta (validar, combinar fuentes, filtrar…).
 *
 * `operator fun invoke()` permite llamarlos como si fueran funciones: `getPopulares()`.
 * (Los agrupamos en un archivo por comodidad de lectura en clase; podrían ir separados.)
 */

/** Observa las películas populares (desde la DB) y permite refrescarlas desde la red. */
class GetPopulares(private val repo: MovieRepository) {
    operator fun invoke(): Flow<List<Movie>> = repo.observarPopulares()
    suspend fun refrescar() = repo.refrescarPopulares()
}

/** Observa solo las favoritas. */
class GetFavoritas(private val repo: MovieRepository) {
    operator fun invoke(): Flow<List<Movie>> = repo.observarFavoritas()
}

/** Busca películas por texto en TMDB. */
class BuscarPeliculas(private val repo: MovieRepository) {
    suspend operator fun invoke(query: String): List<Movie> = repo.buscar(query)
}

/**
 * Detalle de una película:
 *  - [pelicula] = flujo reactivo desde la DB (título, póster y, sobre todo, el estado de favorito).
 *  - [invoke]   = datos extra desde la red (géneros, duración, tagline).
 */
class GetDetalle(private val repo: MovieRepository) {
    fun pelicula(id: Int): Flow<Movie?> = repo.observarPelicula(id)
    suspend operator fun invoke(id: Int): MovieDetail = repo.obtenerDetalle(id)
}

/** Marca o desmarca una película como favorita. */
class MarcarFavorita(private val repo: MovieRepository) {
    suspend operator fun invoke(id: Int, favorita: Boolean) = repo.marcarFavorita(id, favorita)
}
