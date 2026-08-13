package com.jetbrains.kmpapp.domain.usecase

import com.jetbrains.kmpapp.domain.repository.ItemRepository

class ToggleFavorito(
    private val repository: ItemRepository,
) {
    suspend operator fun invoke(id: String) = repository.alternarFavorito(id)
}
