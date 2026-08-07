package com.jetbrains.kmpapp.data.repository

import com.jetbrains.kmpapp.data.local.MovieLocalDataSource
import com.jetbrains.kmpapp.data.remote.TmdbApi
import com.jetbrains.kmpapp.data.remote.toDomain
import com.jetbrains.kmpapp.domain.model.Movie
import com.jetbrains.kmpapp.domain.model.MovieDetail
import com.jetbrains.kmpapp.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow

/**
 * Implementación offline-first del repositorio (capa DATA).
 *
 * Combina las dos fuentes:
 *   - [local]  = base de datos SQLDelight → lo que la UI OBSERVA (fuente de verdad).
 *   - [remote] = API de TMDB (Ktor)       → de dónde REFRESCAMOS los datos.
 *
 * Regla de oro: la UI nunca lee directo de la red; lee de la DB. La red solo alimenta la DB.
 */
class MovieRepositoryImpl(
    private val remote: TmdbApi,
    private val local: MovieLocalDataSource,
) : MovieRepository {

    // ── Lectura reactiva (desde la DB local) ──────────────────────────────────
    override fun observarPopulares(): Flow<List<Movie>> = local.observarTodas()

    override fun observarFavoritas(): Flow<List<Movie>> = local.observarFavoritas()

    override fun observarPelicula(id: Int): Flow<Movie?> = local.observarPorId(id)

    // ── Escritura / red ───────────────────────────────────────────────────────
    override suspend fun refrescarPopulares() {
        // 1) baja de TMDB → 2) DTO a dominio → 3) guarda en la DB (los Flows emiten el cambio).
        val pagina = remote.populares()
        local.guardarTodas(pagina.results.map { it.toDomain() })
    }

    override suspend fun buscar(query: String): List<Movie> =
        remote.buscar(query).results.map { it.toDomain() }

    override suspend fun obtenerDetalle(id: Int): MovieDetail =
        remote.detalle(id).toDomain()

    override suspend fun marcarFavorita(id: Int, favorita: Boolean) =
        local.marcarFavorita(id, favorita)
}
