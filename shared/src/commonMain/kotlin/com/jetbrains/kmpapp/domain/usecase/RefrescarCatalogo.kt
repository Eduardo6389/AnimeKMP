package com.jetbrains.kmpapp.domain.usecase

import com.jetbrains.kmpapp.domain.model.Resultado
import com.jetbrains.kmpapp.domain.repository.ItemRepository

class RefrescarCatalogo(
    private val repository: ItemRepository,
) {
    suspend operator fun invoke(forzar: Boolean = false): Resultado<Boolean> {
        if (!forzar && !repository.cacheExpirada(TTL_MILLIS)) {
            return Resultado.Ok(true)
        }
        return repository.cargarPagina(PAGINA_INICIAL)
    }

    companion object {
        const val TTL_MILLIS = 60 * 60 * 1000L
        private const val PAGINA_INICIAL = 1
    }
}
