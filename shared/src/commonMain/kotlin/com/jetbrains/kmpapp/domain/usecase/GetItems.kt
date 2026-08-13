package com.jetbrains.kmpapp.domain.usecase

import com.jetbrains.kmpapp.domain.model.Orden
import com.jetbrains.kmpapp.domain.repository.ItemRepository

class GetItems(
    private val repository: ItemRepository,
) {
    operator fun invoke(orden: Orden = Orden.METRICA_DESC) = repository.observarCatalogo(orden)

    suspend fun cargarPagina(pagina: Int) = repository.cargarPagina(pagina)
}
