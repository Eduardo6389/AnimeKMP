package com.jetbrains.kmpapp.data.local

import app.cash.sqldelight.Query
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.jetbrains.kmpapp.db.AnimeDb
import com.jetbrains.kmpapp.db.ItemEntity
import com.jetbrains.kmpapp.domain.model.Atributo
import com.jetbrains.kmpapp.domain.model.Item
import com.jetbrains.kmpapp.domain.model.ItemDetalle
import com.jetbrains.kmpapp.domain.model.Orden
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

interface ItemLocalDataSource {
    fun observarCatalogo(orden: Orden): Flow<List<Item>>

    fun observarFavoritos(): Flow<List<Item>>

    suspend fun guardar(
        items: List<Item>,
        descargadoEn: Long,
        reemplazar: Boolean = false,
    )

    suspend fun buscar(query: String): List<Item>

    suspend fun guardarDetalle(detalle: ItemDetalle)

    suspend fun obtenerDetalle(id: String): ItemDetalle?

    suspend fun alternarFavorito(id: String)

    suspend fun limpiarCache()

    suspend fun ultimoTimestamp(): Long?
}

class SqlDelightItemLocalDataSource(
    db: AnimeDb,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ItemLocalDataSource {
    private val queries = db.itemQueries

    override fun observarCatalogo(orden: Orden): Flow<List<Item>> =
        queryOrdenada(orden)
            .asFlow()
            .mapToList(dispatcher)
            .map { filas -> filas.map { it.toItem() } }

    override fun observarFavoritos(): Flow<List<Item>> =
        queries
            .selectFavoritos()
            .asFlow()
            .mapToList(dispatcher)
            .map { filas -> filas.map { it.toItem() } }

    override suspend fun guardar(
        items: List<Item>,
        descargadoEn: Long,
        reemplazar: Boolean,
    ) = withContext(dispatcher) {
        queries.transaction {
            if (reemplazar) limpiarNoFavoritos()
            items.forEach { guardarItem(it, descargadoEn) }
        }
    }

    override suspend fun buscar(query: String): List<Item> =
        withContext(dispatcher) {
            queries.buscarLocal(query).executeAsList().map { it.toItem() }
        }

    override suspend fun guardarDetalle(detalle: ItemDetalle) {
        withContext(dispatcher) {
            queries.upsertDetalle(
                detalle.item.id,
                detalle.descripcion,
                detalle.atributos.encode(),
            )
        }
    }

    override suspend fun obtenerDetalle(id: String): ItemDetalle? =
        withContext(dispatcher) {
            val item =
                queries.selectById(id).executeAsOneOrNull()?.toItem()
                    ?: return@withContext null
            val detalle =
                queries.selectDetalle(id).executeAsOneOrNull()
                    ?: return@withContext null
            ItemDetalle(
                item = item,
                descripcion = detalle.descripcion,
                atributos = detalle.atributos.decodeAtributos(),
                relacionados = emptyList(),
            )
        }

    override suspend fun alternarFavorito(id: String) =
        withContext(dispatcher) {
            val fila =
                queries.selectById(id).executeAsOneOrNull()
                    ?: return@withContext
            val nuevoValor = if (fila.esFavorito == 1L) 0L else 1L
            queries.setFavorito(nuevoValor, id)
        }

    override suspend fun limpiarCache() =
        withContext(dispatcher) {
            queries.transaction {
                limpiarNoFavoritos()
            }
        }

    override suspend fun ultimoTimestamp(): Long? =
        withContext(dispatcher) {
            queries.ultimoTimestamp().executeAsOneOrNull()
        }

    private fun queryOrdenada(orden: Orden): Query<ItemEntity> =
        when (orden) {
            Orden.METRICA_DESC -> queries.selectMetrica()
            Orden.TITULO_ASC -> queries.selectTitulo()
            Orden.FECHA_DESC -> queries.selectFecha()
        }

    private fun guardarItem(
        item: Item,
        descargadoEn: Long,
    ) {
        val tags = item.tags.joinToString(TAG_SEPARATOR)
        insertarSiNoExiste(item, tags, descargadoEn)
        actualizarDatos(item, tags, descargadoEn)
    }

    private fun insertarSiNoExiste(
        item: Item,
        tags: String,
        descargadoEn: Long,
    ) {
        queries.insertIgnore(
            item.id,
            item.titulo,
            item.subtitulo,
            item.imagenUrl,
            item.metrica,
            item.fecha,
            tags,
            descargadoEn,
        )
    }

    private fun actualizarDatos(
        item: Item,
        tags: String,
        descargadoEn: Long,
    ) {
        queries.updateData(
            item.titulo,
            item.subtitulo,
            item.imagenUrl,
            item.metrica,
            item.fecha,
            tags,
            descargadoEn,
            item.id,
        )
    }

    private fun limpiarNoFavoritos() {
        queries.deleteDetallesNoFavoritos()
        queries.deleteCache()
    }
}

private fun ItemEntity.toItem(): Item =
    Item(
        id = id,
        titulo = titulo,
        subtitulo = subtitulo,
        imagenUrl = imagenUrl,
        metrica = metrica,
        fecha = fecha,
        tags = tags.split(TAG_SEPARATOR).filter { it.isNotBlank() },
    )

private fun List<Atributo>.encode(): String =
    joinToString(ATTR_SEPARATOR) {
        "${it.etiqueta}$VALUE_SEPARATOR${it.valor}"
    }

private fun String.decodeAtributos(): List<Atributo> =
    split(ATTR_SEPARATOR).mapNotNull { valor ->
        val partes = valor.split(VALUE_SEPARATOR, limit = 2)
        if (partes.size == 2) Atributo(partes[0], partes[1]) else null
    }

private const val TAG_SEPARATOR = "|"
private const val ATTR_SEPARATOR = "\u001E"
private const val VALUE_SEPARATOR = "\u001F"
