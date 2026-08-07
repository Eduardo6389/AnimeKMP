package com.jetbrains.kmpapp

// Data class: guarda datos + te da copy() e igualdad gratis
data class Pelicula(val id: Int, val titulo: String, val rating: Double)

// Sealed: estados EXCLUSIVOS (o cargando, o éxito, o error). Lo usaremos TODO el curso.
sealed interface Estado<out T> {
    data object Cargando : Estado<Nothing>
    data class Exito<T>(val data: T) : Estado<T>
    data class Error(val mensaje: String) : Estado<Nothing>
}

sealed interface Resultado<out T> {
    data class Ok<T>(val valor: T) : Resultado<T>
    data class Fallo(val mensaje: String) : Resultado<Nothing>
}

fun manejarResultado(res: Resultado<String>) {
    when (res) {
        is Resultado.Ok -> println("Exito: ${res.valor}")
        is Resultado.Fallo -> println("Error: ${res.mensaje}")
    }
}

// Función de extensión: le agrego un método a un tipo existente
fun Double.aEstrellas(): String = "★".repeat((this / 2).toInt())

fun String.esEmailValido(): Boolean = this.contains("@") && this.contains(".")

fun Int.esPar(): Boolean = this % 2 == 0

fun <T> List<T>.segundoElemento(): T? = if (this.size >= 2) this[1] else null

fun String.capitalizar(): String = replaceFirstChar { it.uppercase() }

fun Int.diasAMilisegundos(): Long = this * 24L * 60 * 60 * 1000

fun main() {
    println(" Probando Data Classes ")
    val p = Pelicula(1, "Dune", 8.4)
    val mejor = p.copy(rating = 9.0)
    println("${p.titulo} tiene rating de ${p.rating.aEstrellas()}")
    println("Copia de pelicula: $mejor")

    println("\n Probando Sealed Interfaces")
    val resOk = Resultado.Ok("Todo salio bien")
    val resError = Resultado.Fallo("Algo fallo")
    manejarResultado(resOk)
    manejarResultado(resError)

    println("\n Probando Funciones de Extension ")
    println("Email 'hola@mundo.com' valido: ${"hola@mundo.com".esEmailValido()}")
    println("¿Es 10 par?: ${10.esPar()}")
    
    val nombres = listOf("Ana", "Ruby", "Carla")
    println("El segundo nombre es: ${nombres.segundoElemento()}")
    
    println("Texto capitalizado: ${"kotlin es genial".capitalizar()}")
    println("3 dias a milisegundos: ${3.diasAMilisegundos()}")
}
