package com.jetbrains.kmpapp.presentation

/**
 * Convierte una excepción técnica en un mensaje entendible para el usuario.
 *
 * Nota de arquitectura: la capa de presentación NO importa clases de Ktor (para no acoplarse a
 * la librería de red). Por eso miramos el nombre/mensaje de la excepción en vez de su tipo exacto.
 * Es "suficiente y honesto" para mostrar algo útil; la causa técnica sigue en el `printStackTrace`.
 */
fun Throwable.mensajeAmigable(): String {
    val texto = (this::class.simpleName.orEmpty() + " " + (message ?: "")).lowercase()
    return when {
        "401" in texto || "unauthorized" in texto ->
            "Tu API key de TMDB es inválida o falta (revisa local.properties)."
        "clientrequest" in texto || "404" in texto ->
            "No se encontró el recurso solicitado."
        "serverresponse" in texto || "500" in texto ->
            "El servidor de TMDB falló. Intenta de nuevo más tarde."
        else ->
            "No se pudieron cargar las películas. Revisa tu conexión a internet."
    }
}
