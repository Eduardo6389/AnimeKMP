package com.jetbrains.kmpapp
import com.jetbrains.kmpapp.util.Contador
import kotlin.test.Test
import kotlin.test.assertEquals

class ContadorTest {
 @Test
 fun incrementa_de_cero_a_dos() {
 val c = Contador()
 c.incrementar(); c.incrementar()
 assertEquals(2, c.valor.value, "El contador debería valer 2 después de incrementar dos veces")
  }
}