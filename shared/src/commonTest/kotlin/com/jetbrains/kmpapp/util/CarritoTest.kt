package com.jetbrains.kmpapp.util

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CarritoTest {

    @Test
    fun testAgregarItem() = runTest {
        val carrito = Carrito()
        carrito.agregar("Manzana")
        assertEquals(1, carrito.items.value.size, "El tamaño del carrito debería ser 1 después de agregar un item")
        assertEquals("Manzana", carrito.items.value.first(), "El primer elemento debería ser 'Manzana'")
    }

    @Test
    fun testQuitarItem() = runTest {
        val carrito = Carrito()
        carrito.agregar("Pan Integral")
        carrito.quitar("Pan Integral")
        assertTrue(carrito.items.value.isEmpty(), "El carrito debería estar vacío después de quitar el único item")
    }

    @Test
    fun testTotalItemsSeActualiza() = runTest {
        val carrito = Carrito()
        val totalFlow = carrito.totalItems
        
        assertEquals(0, totalFlow.first(), "El total inicial debería ser 0")
        
        carrito.agregar("Leche")
        assertEquals(1, totalFlow.first(), "El total debería actualizarse a 1 tras agregar un item")
    }
}
