package com.jetbrains.kmpapp.util

import kotlin.test.Test
import kotlin.test.assertTrue

class RelojTest {

    @Test
    fun testEpochMillisDevuelveValorPositivo() {
        val tiempo = epochMillis()
        assertTrue(tiempo > 0, "El tiempo epoch debe ser mayor que cero")
    }

    @Test
    fun testRelojAhoraFunciona() {
        val reloj = Reloj()
        val ahora = reloj.ahora()
        assertTrue(ahora > 0, "Reloj.ahora() debe devolver un valor positivo")
    }

    @Test
    fun testNuevoUuidNoEstaVacio() {
        val uuid = nuevoUuid()
        assertTrue(uuid.isNotEmpty(), "El UUID no debe estar vacío")
    }
}
