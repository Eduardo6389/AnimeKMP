package com.jetbrains.kmpapp.domain.repository

import com.jetbrains.kmpapp.domain.model.Movie
import com.jetbrains.kmpapp.domain.model.MovieDetail
import kotlinx.coroutines.flow.Flow

/**
 * Contrato del repositorio de películas (capa DOMAIN).
 *
 * Clave de Clean Architecture: esto vive en `domain` y NO sabe de Ktor ni de SQLDelight.
 * Los ViewModels y casos de uso dependen de ESTA interfaz, no de la implementación concreta.
 * Así podemos cambiar la fuente de datos (o inyectar un fake en tests) sin tocar la lógica.
 *
 * Patrón offline-first:
 *  - Las funciones `observar…` devuelven un Flow que lee de la base de datos local (fuente de
 *    verdad para la UI): la app muestra datos aunque no haya internet.
 *  - `refrescar…` baja de la red y ACTUALIZA la base de datos; los Flows emiten solos el cambio.
 */
interface MovieRepository {

    /** Populares guardadas localmente (se refrescan con [refrescarPopulares]). */
    fun observarPopulares(): Flow<List<Movie>>

    /** Solo las marcadas como favoritas. */
    fun observarFavoritas(): Flow<List<Movie>>

    /** Una película concreta (para el detalle y su botón de favorito), reactiva. */
    fun observarPelicula(id: Int): Flow<Movie?>

    /** Baja populares de TMDB y las guarda en la DB local. */
    suspend fun refrescarPopulares()

    /** Búsqueda en TMDB (resultados en vivo, no se cachean). */
    suspend fun buscar(query: String): List<Movie>

    /** Detalle completo (géneros, duración…) desde la red. */
    suspend fun obtenerDetalle(id: Int): MovieDetail

    /** Marca/desmarca favorito en la DB local. */
    suspend fun marcarFavorita(id: Int, favorita: Boolean)
}
