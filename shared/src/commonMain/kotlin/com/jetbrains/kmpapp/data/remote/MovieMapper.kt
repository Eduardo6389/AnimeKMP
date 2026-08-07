package com.jetbrains.kmpapp.data.remote

import com.jetbrains.kmpapp.config.CineBuildConfig
import com.jetbrains.kmpapp.data.dto.MovieDetailDto
import com.jetbrains.kmpapp.data.dto.MovieDto
import com.jetbrains.kmpapp.domain.model.Movie
import com.jetbrains.kmpapp.domain.model.MovieDetail

/**
 * MAPPERS: convierten los DTO (JSON crudo) en modelos de dominio (limpios).
 * Aquí es donde "traducimos" `poster_path` → URL completa y `release_date` → año.
 *
 * TMDB solo manda el tramo final del póster (ej. "/abc.jpg"); hay que anteponer el host
 * y el tamaño (w500) para tener una URL usable. Si no hay póster, devolvemos null.
 */
private fun posterUrlCompleta(path: String?): String? =
    path?.let { CineBuildConfig.TMDB_IMAGE_BASE_URL + it }

/** "2024-03-01" → "2024".  null/"" → "". `take(4)` toma los primeros 4 caracteres. */
private fun anioDe(releaseDate: String?): String =
    releaseDate?.take(4).orEmpty()

/** DTO de lista → modelo de dominio. */
fun MovieDto.toDomain(esFavorita: Boolean = false): Movie = Movie(
    id = id,
    titulo = title,
    overview = overview,
    rating = voteAverage,
    posterUrl = posterUrlCompleta(posterPath),
    anio = anioDe(releaseDate),
    esFavorita = esFavorita,
)

/** DTO de detalle → modelo de dominio (con géneros, duración, etc.). */
fun MovieDetailDto.toDomain(esFavorita: Boolean = false): MovieDetail = MovieDetail(
    id = id,
    titulo = title,
    overview = overview,
    rating = voteAverage,
    posterUrl = posterUrlCompleta(posterPath),
    backdropUrl = posterUrlCompleta(backdropPath),
    anio = anioDe(releaseDate),
    duracionMin = runtime,
    generos = genres.map { it.name },      // List<GenreDto> → List<String>
    tagline = tagline?.ifBlank { null },   // TMDB a veces manda "" en vez de faltar
    esFavorita = esFavorita,
)
