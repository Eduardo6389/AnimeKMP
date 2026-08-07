package com.jetbrains.kmpapp.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTOs (Data Transfer Objects): copian EXACTAMENTE la forma del JSON de TMDB.
 *
 *  - @Serializable  → le dice a kotlinx.serialization que sepa leer/escribir esta clase.
 *  - @SerialName    → conecta el nombre del JSON (ej. "vote_average") con nuestra variable
 *                     Kotlin (voteAverage). Sin él, el nombre debe coincidir letra por letra.
 *
 * Los campos opcionales llevan `= null` para que, si TMDB no los manda, no truene el parseo.
 */

/** Respuesta paginada: /movie/popular, /movie/top_rated, /search/movie, etc. */
@Serializable
data class MoviePageDto(
    val page: Int,
    val results: List<MovieDto>,
    @SerialName("total_pages") val totalPages: Int = 1,
    @SerialName("total_results") val totalResults: Int = 0,
)

/** Una película tal como llega en una LISTA. */
@Serializable
data class MovieDto(
    val id: Int,
    val title: String = "",
    val overview: String = "",
    @SerialName("vote_average") val voteAverage: Double = 0.0,
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("backdrop_path") val backdropPath: String? = null,
    @SerialName("release_date") val releaseDate: String? = null,
)

/** Respuesta de DETALLE: /movie/{id}. Trae campos extra que la lista no tiene. */
@Serializable
data class MovieDetailDto(
    val id: Int,
    val title: String = "",
    val overview: String = "",
    @SerialName("vote_average") val voteAverage: Double = 0.0,
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("backdrop_path") val backdropPath: String? = null,
    @SerialName("release_date") val releaseDate: String? = null,
    val runtime: Int? = null,
    val tagline: String? = null,
    val genres: List<GenreDto> = emptyList(),
)

/** Un género ("Acción", "Drama"...). Viene anidado dentro del detalle. */
@Serializable
data class GenreDto(
    val id: Int,
    val name: String,
)
