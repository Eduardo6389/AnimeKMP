package com.jetbrains.kmpapp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AnimePageDto(
    @SerialName("data")
    val datos: List<AnimeDto>,
    @SerialName("pagination")
    val paginacion: PaginationDto = PaginationDto(),
)

@Serializable
data class AnimeResponseDto(
    @SerialName("data")
    val dato: AnimeDto,
)

@Serializable
data class PaginationDto(
    @SerialName("has_next_page")
    val haySiguiente: Boolean = false,
)

@Serializable
data class AnimeDto(
    @SerialName("mal_id")
    val id: Int,
    @SerialName("title")
    val titulo: String,
    @SerialName("synopsis")
    val sinopsis: String? = null,
    @SerialName("score")
    val puntuacion: Double? = null,
    @SerialName("year")
    val anio: Int? = null,
    @SerialName("type")
    val tipo: String? = null,
    @SerialName("episodes")
    val episodios: Int? = null,
    @SerialName("status")
    val estado: String? = null,
    @SerialName("duration")
    val duracion: String? = null,
    @SerialName("rating")
    val clasificacion: String? = null,
    @SerialName("images")
    val imagenes: AnimeImagesDto? = null,
    @SerialName("genres")
    val generos: List<AnimeGenreDto> = emptyList(),
)

@Serializable
data class AnimeImagesDto(
    val jpg: AnimeJpgDto? = null,
)

@Serializable
data class AnimeJpgDto(
    @SerialName("image_url")
    val imagenUrl: String? = null,
    @SerialName("large_image_url")
    val imagenGrandeUrl: String? = null,
)

@Serializable
data class AnimeGenreDto(
    @SerialName("name")
    val nombre: String,
)
