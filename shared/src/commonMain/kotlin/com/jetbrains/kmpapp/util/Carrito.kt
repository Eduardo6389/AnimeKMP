package com.jetbrains.kmpapp.util

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*


 // Paso 2: Corrutinas/Flow


// 1. Flow de búsqueda con debounce y distinctUntilChanged
@OptIn(FlowPreview::class)
fun busquedaFlow(query: Flow<String>): Flow<String> = 
    query.debounce(300)
         .distinctUntilChanged()

// 2. StateFlow de un carrito
class Carrito {
    private val _items = MutableStateFlow<List<String>>(emptyList())
    val items: StateFlow<List<String>> = _items.asStateFlow()

    // Flujo derivado que da el total de ítems
    val totalItems: Flow<Int> = _items.map { it.size }

    fun agregar(producto: String) {
        _items.update { it + producto }
    }

    fun quitar(producto: String) {
        _items.update { it - producto }
    }
}
