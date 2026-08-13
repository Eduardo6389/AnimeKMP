package com.jetbrains.kmpapp.domain.usecase

import com.jetbrains.kmpapp.domain.repository.ItemRepository

class GetFavoritos(
    private val repository: ItemRepository,
) {
    operator fun invoke() = repository.observarFavoritos()
}
