package com.jetbrains.kmpapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.jetbrains.kmpapp.domain.model.AppError

@Composable
fun ErrorView(
    error: AppError,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(mensajeError(error))
        Button(onClick = onRetry) { Text("Reintentar") }
    }
}

fun mensajeError(error: AppError): String =
    when (error) {
        AppError.SinConexion -> "Sin conexión"
        AppError.Timeout -> "La solicitud tardó demasiado"
        is AppError.HttpCliente -> "Error de solicitud (${error.codigo})"
        is AppError.HttpServidor -> "Error del servidor (${error.codigo})"
        is AppError.Parseo -> "No se pudieron leer los datos"
        is AppError.Desconocido -> "Ocurrió un error"
    }
