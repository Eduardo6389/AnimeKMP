package com.jetbrains.kmpapp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AnimePageDto(
    val data: List<AnimeDto>,
)

@Serializable
data class AnimeResponseDto(
    val data: AnimeDto,
)

@Serializable
data class AnimeDto(
    @SerialName("mal_id")
    val id: Int,
    val title: String,
    val synopsis: String? = null,
    val score: Double? = null,
    val year: Int? = null,
    val images: AnimeImagesDto? = null,
    val genres: List<AnimeGenreDto> = emptyList(),
)

@Serializable
data class AnimeImagesDto(
    val jpg: AnimeJpgDto? = null,
)

@Serializable
data class AnimeJpgDto(
    @SerialName("image_url")
    val imageUrl: String? = null,
)

@Serializable
data class AnimeGenreDto(
    val name: String,
)

