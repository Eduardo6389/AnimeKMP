package com.jetbrains.kmpapp.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.jetbrains.kmpapp.db.CineDb
import com.jetbrains.kmpapp.db.MovieEntity
import com.jetbrains.kmpapp.domain.model.Movie
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Fuente de datos LOCAL (la base de datos). Se declara como interfaz para poder sustituirla por
 * una versión "de mentira" (fake en memoria) en los tests del repositorio, sin SQLite real.
 */
interface MovieLocalDataSource {
    fun observarTodas(): Flow<List<Movie>>
    fun observarFavoritas(): Flow<List<Movie>>
    fun observarPorId(id: Int): Flow<Movie?>
    suspend fun guardarTodas(movies: List<Movie>)
    suspend fun marcarFavorita(id: Int, favorita: Boolean)
}

/**
 * Implementación real con SQLDelight.
 *
 * - `.asFlow().mapToList(...)` convierte una query en un Flow que RE-EMITE cada vez que la tabla
 *   cambia: esto es lo que hace que la UI se actualice sola (offline-first + reactivo).
 * - Las escrituras van en un dispatcher de fondo (no en el hilo principal).
 *
 * SQLite guarda enteros como Long; nuestro dominio usa Int (los id de TMDB caben de sobra),
 * por eso convertimos con `.toInt()` / `.toLong()`. Y el favorito es 0/1 (no hay Boolean en SQLite).
 */
class SqlDelightMovieLocalDataSource(
    db: CineDb,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : MovieLocalDataSource {

    private val queries = db.movieQueries

    override fun observarTodas(): Flow<List<Movie>> =
        queries.selectAll().asFlow().mapToList(dispatcher).map { fila -> fila.map { it.toDomain() } }

    override fun observarFavoritas(): Flow<List<Movie>> =
        queries.selectFavorites().asFlow().mapToList(dispatcher).map { fila -> fila.map { it.toDomain() } }

    override fun observarPorId(id: Int): Flow<Movie?> =
        queries.selectById(id.toLong()).asFlow().mapToOneOrNull(dispatcher).map { it?.toDomain() }

    override suspend fun guardarTodas(movies: List<Movie>) = withContext(dispatcher) {
        // Una sola transacción para todas las inserciones (más rápido y atómico).
        // insertIgnore crea la fila si no existe; updateData refresca los datos SIN tocar isFavorite.
        queries.transaction {
            movies.forEach { m ->
                queries.insertIgnore(
                    id = m.id.toLong(),
                    title = m.titulo,
                    overview = m.overview,
                    rating = m.rating,
                    posterUrl = m.posterUrl,
                    year = m.anio,
                )
                queries.updateData(
                    title = m.titulo,
                    overview = m.overview,
                    rating = m.rating,
                    posterUrl = m.posterUrl,
                    year = m.anio,
                    id = m.id.toLong(),
                )
            }
        }
    }

    override suspend fun marcarFavorita(id: Int, favorita: Boolean) {
        withContext(dispatcher) {
            queries.setFavorite(isFavorite = if (favorita) 1L else 0L, id = id.toLong())
        }
    }
}

/** MovieEntity (fila de la DB) → Movie (dominio). */
private fun MovieEntity.toDomain(): Movie = Movie(
    id = id.toInt(),
    titulo = title,
    overview = overview,
    rating = rating,
    posterUrl = posterUrl,
    anio = year,
    esFavorita = isFavorite == 1L,
)
