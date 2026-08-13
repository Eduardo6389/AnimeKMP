package com.jetbrains.kmpapp.data.repository

import com.jetbrains.kmpapp.data.local.ItemLocalDataSource
import com.jetbrains.kmpapp.data.remote.ApiClient
import com.jetbrains.kmpapp.data.remote.dto.AnimePageDto
import com.jetbrains.kmpapp.data.remote.llamadaSegura
import com.jetbrains.kmpapp.data.remote.toDetalle
import com.jetbrains.kmpapp.data.remote.toItem
import com.jetbrains.kmpapp.domain.model.AppError
import com.jetbrains.kmpapp.domain.model.Item
import com.jetbrains.kmpapp.domain.model.ItemDetalle
import com.jetbrains.kmpapp.domain.model.Orden
import com.jetbrains.kmpapp.domain.model.Resultado
import com.jetbrains.kmpapp.domain.repository.ItemRepository
import com.jetbrains.kmpapp.platform.Conectividad
import com.jetbrains.kmpapp.platform.Reloj
import kotlinx.coroutines.flow.Flow

class ItemRepositoryImpl(
    private val api: ApiClient,
    private val local: ItemLocalDataSource,
    private val conectividad: Conectividad,
) : ItemRepository {
    override fun observarCatalogo(orden: Orden): Flow<List<Item>> = local.observarCatalogo(orden)

    override fun observarFavoritos(): Flow<List<Item>> = local.observarFavoritos()

    override suspend fun cargarPagina(pagina: Int): Resultado<Boolean> {
        if (!conectividad.conectado.value) {
            return Resultado.Fallo(AppError.SinConexion)
        }
        return when (val resultado = llamadaSegura { api.listado(pagina) }) {
            is Resultado.Ok -> guardarPagina(resultado.valor, pagina)
            is Resultado.Fallo -> resultado
        }
    }

    override suspend fun buscar(query: String): Resultado<List<Item>> {
        if (query.isBlank()) return Resultado.Ok(emptyList())
        if (!conectividad.conectado.value) return buscarLocal(query)

        return when (val resultado = llamadaSegura { api.buscar(query) }) {
            is Resultado.Ok -> guardarBusqueda(resultado.valor)
            is Resultado.Fallo -> buscarLocal(query, resultado)
        }
    }

    override suspend fun obtenerDetalle(id: String): Resultado<ItemDetalle> {
        if (!conectividad.conectado.value) return detalleLocal(id)
        return when (val resultado = llamadaSegura { api.detalle(id) }) {
            is Resultado.Ok -> guardarDetalle(resultado.valor.dato.toDetalle())
            is Resultado.Fallo -> detalleLocal(id, resultado)
        }
    }

    override suspend fun alternarFavorito(id: String) = local.alternarFavorito(id)

    override suspend fun limpiarCache() = local.limpiarCache()

    override suspend fun cacheExpirada(ttlMillis: Long): Boolean {
        val ultimo = local.ultimoTimestamp() ?: return true
        return Reloj.ahoraMillis() - ultimo > ttlMillis
    }

    private suspend fun guardarPagina(
        pagina: AnimePageDto,
        numero: Int,
    ): Resultado<Boolean> {
        val items = pagina.datos.map { it.toItem() }
        local.guardar(
            items = items,
            descargadoEn = Reloj.ahoraMillis(),
            reemplazar = numero == PAGINA_INICIAL,
        )
        return Resultado.Ok(pagina.paginacion.haySiguiente)
    }

    private suspend fun guardarBusqueda(pagina: AnimePageDto): Resultado<List<Item>> {
        val items = pagina.datos.map { it.toItem() }
        local.guardar(items, Reloj.ahoraMillis())
        return Resultado.Ok(items)
    }

    private suspend fun guardarDetalle(detalle: ItemDetalle): Resultado<ItemDetalle> {
        local.guardar(
            items = listOf(detalle.item),
            descargadoEn = Reloj.ahoraMillis(),
        )
        local.guardarDetalle(detalle)
        return Resultado.Ok(detalle)
    }

    private suspend fun buscarLocal(
        query: String,
        fallo: Resultado.Fallo = Resultado.Fallo(AppError.SinConexion),
    ): Resultado<List<Item>> {
        val items = local.buscar(query)
        return if (items.isEmpty()) fallo else Resultado.Ok(items)
    }

    private suspend fun detalleLocal(
        id: String,
        fallo: Resultado.Fallo = Resultado.Fallo(AppError.SinConexion),
    ): Resultado<ItemDetalle> {
        val detalle = local.obtenerDetalle(id)
        return if (detalle == null) fallo else Resultado.Ok(detalle)
    }

    companion object {
        private const val PAGINA_INICIAL = 1
    }
}
