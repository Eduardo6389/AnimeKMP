package com.jetbrains.kmpapp.presentation

import com.jetbrains.kmpapp.domain.model.AppError

fun mensajeAppError(error: AppError): String =
    when (error) {
        AppError.SinConexion -> "Sin conexión"
        AppError.Timeout -> "La solicitud tardó demasiado"
        is AppError.HttpCliente -> "Error de solicitud (${error.codigo})"
        is AppError.HttpServidor -> "Error del servidor (${error.codigo})"
        is AppError.Parseo -> "No se pudieron leer los datos"
        is AppError.Desconocido -> "Ocurrió un error"
    }
