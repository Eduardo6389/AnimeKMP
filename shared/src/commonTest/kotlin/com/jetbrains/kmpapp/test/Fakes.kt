package com.jetbrains.kmpapp.test

import com.jetbrains.kmpapp.data.local.ItemLocalDataSource
import com.jetbrains.kmpapp.domain.model.AppError
import com.jetbrains.kmpapp.domain.model.Item
import com.jetbrains.kmpapp.domain.model.ItemDetalle
import com.jetbrains.kmpapp.domain.model.Orden
import com.jetbrains.kmpapp.domain.model.Resultado
import com.jetbrains.kmpapp.domain.repository.ItemRepository
import com.jetbrains.kmpapp.platform.Conectividad
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

fun item(
    id: String = "1",
    titulo: String = "Anime",
    metrica: Double? = 8.0,
): Item =
    Item(
        id = id,
        titulo = titulo,
        subtitulo = "TV",
        imagenUrl = null,
        metrica = metrica,
        fecha = "2024",
        tags = listOf("Action"),
    )

class FakeConectividad(
    conectadoInicial: Boolean = true,
) : Conectividad {
    private val estado = MutableStateFlow(conectadoInicial)
    override val conectado: StateFlow<Boolean> = estado

    fun cambiar(valor: Boolean) {
        estado.value = valor
    }
}

class FakeItemRepository : ItemRepository {
    val catalogo = MutableStateFlow<List<Item>>(emptyList())
    val favoritos = MutableStateFlow<List<Item>>(emptyList())
    var cargarResultado: Resultado<Boolean> = Resultado.Ok(false)
    var buscarResultado: Resultado<List<Item>> = Resultado.Ok(emptyList())
    var detalleResultado: Resultado<ItemDetalle> =
        Resultado.Fallo(AppError.Desconocido("Sin detalle"))
    var expirada = true
    var paginaPedida = 0
    var busquedas = 0
    var ultimoQuery = ""
    var favoritosCambios = 0
    var cacheLimpia = false

    override fun observarCatalogo(orden: Orden): Flow<List<Item>> = catalogo

    override fun observarFavoritos(): Flow<List<Item>> = favoritos

    override suspend fun cargarPagina(pagina: Int): Resultado<Boolean> {
        paginaPedida = pagina
        return cargarResultado
    }

    override suspend fun buscar(query: String): Resultado<List<Item>> {
        busquedas += 1
        ultimoQuery = query
        return buscarResultado
    }

    override suspend fun obtenerDetalle(id: String): Resultado<ItemDetalle> = detalleResultado

    override suspend fun alternarFavorito(id: String) {
        favoritosCambios += 1
        val actual = catalogo.value.firstOrNull { it.id == id } ?: item(id)
        favoritos.value =
            if (favoritos.value.any { it.id == id }) {
                favoritos.value.filterNot { it.id == id }
            } else {
                favoritos.value + actual
            }
    }

    override suspend fun limpiarCache() {
        cacheLimpia = true
    }

    override suspend fun cacheExpirada(ttlMillis: Long): Boolean = expirada
}

class FakeLocalDataSource : ItemLocalDataSource {
    val catalogo = MutableStateFlow<List<Item>>(emptyList())
    val favoritos = MutableStateFlow<List<Item>>(emptyList())
    var detalle: ItemDetalle? = null
    var timestamp: Long? = null
    var ultimaBusqueda: List<Item> = emptyList()
    var reemplazo = false

    override fun observarCatalogo(orden: Orden): Flow<List<Item>> = catalogo

    override fun observarFavoritos(): Flow<List<Item>> = favoritos

    override suspend fun guardar(
        items: List<Item>,
        descargadoEn: Long,
        reemplazar: Boolean,
    ) {
        this.reemplazo = reemplazar
        timestamp = descargadoEn
        catalogo.value =
            if (reemplazar) items else (catalogo.value + items).distinctBy { it.id }
    }

    override suspend fun buscar(query: String): List<Item> = ultimaBusqueda

    override suspend fun guardarDetalle(detalle: ItemDetalle) {
        this.detalle = detalle
    }

    override suspend fun obtenerDetalle(id: String): ItemDetalle? = detalle

    override suspend fun alternarFavorito(id: String) {
        val actual = catalogo.value.firstOrNull { it.id == id } ?: return
        favoritos.value =
            if (favoritos.value.any { it.id == id }) {
                favoritos.value.filterNot { it.id == id }
            } else {
                favoritos.value + actual
            }
    }

    override suspend fun limpiarCache() {
        catalogo.value = favoritos.value
    }

    override suspend fun ultimoTimestamp(): Long? = timestamp
}
