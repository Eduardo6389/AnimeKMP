package com.jetbrains.kmpapp.domain.repository

import com.jetbrains.kmpapp.domain.model.Item
import com.jetbrains.kmpapp.domain.model.ItemDetalle
import com.jetbrains.kmpapp.domain.model.Orden
import com.jetbrains.kmpapp.domain.model.Resultado
import kotlinx.coroutines.flow.Flow

interface ItemRepository {
    fun observarCatalogo(orden: Orden = Orden.METRICA_DESC): Flow<List<Item>>

    fun observarFavoritos(): Flow<List<Item>>

    suspend fun cargarPagina(pagina: Int): Resultado<Boolean>

    suspend fun buscar(query: String): Resultado<List<Item>>

    suspend fun obtenerDetalle(id: String): Resultado<ItemDetalle>

    suspend fun alternarFavorito(id: String)

    suspend fun limpiarCache()

    suspend fun cacheExpirada(ttlMillis: Long): Boolean
}
