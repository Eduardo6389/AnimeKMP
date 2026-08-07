package com.jetbrains.kmpapp.domain.model

data class ItemDetalle(
    val item: Item,
    val descripcion: String,
    val atributos: List<Atributo>,
    val relacionados: List<Item>,
)
