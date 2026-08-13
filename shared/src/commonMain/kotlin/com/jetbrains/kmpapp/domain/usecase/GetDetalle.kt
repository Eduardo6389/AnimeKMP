package com.jetbrains.kmpapp.domain.usecase

import com.jetbrains.kmpapp.domain.repository.ItemRepository

class GetDetalle(
    private val repository: ItemRepository,
) {
    suspend operator fun invoke(id: String) = repository.obtenerDetalle(id)
}
