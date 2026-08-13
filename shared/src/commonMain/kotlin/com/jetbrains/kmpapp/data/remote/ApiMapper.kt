package com.jetbrains.kmpapp.data.remote

import com.jetbrains.kmpapp.data.remote.dto.AnimeDto
import com.jetbrains.kmpapp.domain.model.Atributo
import com.jetbrains.kmpapp.domain.model.Item
import com.jetbrains.kmpapp.domain.model.ItemDetalle

fun AnimeDto.toItem(): Item =
    Item(
        id = id.toString(),
        titulo = titulo,
        subtitulo = tipo,
        imagenUrl = imagenes?.jpg?.imagenUrl,
        metrica = puntuacion,
        fecha = anio?.toString(),
        tags = generos.take(MAX_TAGS).map { it.nombre },
    )

fun AnimeDto.toDetalle(): ItemDetalle =
    ItemDetalle(
        item =
            toItem().copy(
                imagenUrl = imagenes?.jpg?.imagenGrandeUrl ?: imagenes?.jpg?.imagenUrl,
            ),
        descripcion = sinopsis.orEmpty(),
        atributos =
            listOf(
                Atributo("Tipo", tipo ?: SIN_DATO),
                Atributo("Episodios", episodios?.toString() ?: SIN_DATO),
                Atributo("Estado", estado ?: SIN_DATO),
                Atributo("Duración", duracion ?: SIN_DATO),
                Atributo("Clasificación", clasificacion ?: SIN_DATO),
            ),
        relacionados = emptyList(),
    )

private const val MAX_TAGS = 5
private const val SIN_DATO = "Sin dato"
