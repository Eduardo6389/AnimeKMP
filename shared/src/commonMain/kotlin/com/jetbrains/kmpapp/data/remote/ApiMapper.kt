package com.jetbrains.kmpapp.data.remote

import com.jetbrains.kmpapp.data.remote.dto.AnimeDto
import com.jetbrains.kmpapp.domain.model.Atributo
import com.jetbrains.kmpapp.domain.model.Item
import com.jetbrains.kmpapp.domain.model.ItemDetalle

fun AnimeDto.toItem(): Item =
    Item(
        id = id.toString(),
        titulo = title,
        subtitulo = year?.toString(),
        imagenUrl = images?.jpg?.imageUrl,
        metrica = score,
        fecha = year?.toString(),
        tags = genres.take(5).map { it.name },
    )

fun AnimeDto.toDetalle(): ItemDetalle =
    ItemDetalle(
        item = toItem(),
        descripcion = synopsis.orEmpty(),
        atributos = listOf(
            Atributo("Puntuación", score?.toString() ?: "Sin dato"),
            Atributo("Año", year?.toString() ?: "Sin dato"),
            Atributo(
                "Géneros",
                genres.joinToString { it.name }.ifEmpty { "Sin dato" },
            ),
            Atributo("ID MyAnimeList", id.toString()),
        ),
        relacionados = emptyList(),
    )