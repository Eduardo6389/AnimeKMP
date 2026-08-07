package com.jetbrains.kmpapp.domain.model

data class Item(
    val id: String,
    val titulo: String,
    val subtitulo: String?,
    val imagenUrl: String?,
    val metrica: Double?,
    val fecha: String?,
    val tags: List<String>,
)

