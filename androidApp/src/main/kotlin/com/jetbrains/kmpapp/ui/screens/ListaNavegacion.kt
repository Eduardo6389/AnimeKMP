package com.jetbrains.kmpapp.ui.screens

data class ListaNavegacion(
    val detalle: (String) -> Unit,
    val favoritos: () -> Unit,
    val ajustes: () -> Unit,
)
