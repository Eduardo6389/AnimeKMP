package com.jetbrains.kmpapp.domain.model

/**
 * Modelo de DOMINIO de una película (lo que usa la app: ViewModels y UI).
 *
 * ¿Por qué no usar el DTO directamente?
 *  - El DTO (MovieDto) refleja el JSON crudo de TMDB (campos como `poster_path`, `vote_average`).
 *  - Este modelo es "limpio": nombres claros, `posterUrl` ya armada y lista para Coil,
 *    y un flag `esFavorita` que la red no conoce (viene de nuestra base de datos local).
 *
 * Separar DTO ↔ dominio nos deja cambiar la API sin tocar la UI (y viceversa).
 */
data class Movie(
    val id: Int,
    val titulo: String,
    val overview: String,
    val rating: Double,
    /** URL COMPLETA del póster (o null si TMDB no tiene imagen). Coil la consume tal cual. */
    val posterUrl: String?,
    /** Año (extraído de release_date "2024-03-01" → "2024"). "" si no hay fecha. */
    val anio: String,
    /** Marca de favorito. La red nunca la trae: la pone la DB local (Sesión 4). */
    val esFavorita: Boolean = false,
)
