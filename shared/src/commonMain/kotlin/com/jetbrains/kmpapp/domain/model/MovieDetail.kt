package com.jetbrains.kmpapp.domain.model

/**
 * Modelo de DOMINIO para la pantalla de DETALLE.
 *
 * El endpoint `movie/{id}` de TMDB trae más datos que la lista (duración, géneros, tagline),
 * por eso usamos un modelo aparte en vez de reutilizar [Movie]. La UI de detalle pinta esto.
 */
data class MovieDetail(
    val id: Int,
    val titulo: String,
    val overview: String,
    val rating: Double,
    val posterUrl: String?,
    /** Imagen ancha de fondo (backdrop). Útil como cabecera del detalle. */
    val backdropUrl: String?,
    val anio: String,
    /** Duración en minutos (runtime). null si TMDB no la reporta. */
    val duracionMin: Int?,
    val generos: List<String>,
    val tagline: String?,
    val esFavorita: Boolean = false,
)
