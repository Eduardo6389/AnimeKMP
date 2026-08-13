package com.jetbrains.kmpapp.domain.usecase

import com.jetbrains.kmpapp.domain.repository.ItemRepository

class BuscarItems(
    private val repository: ItemRepository,
) {
    suspend operator fun invoke(query: String) = repository.buscar(query)
}
