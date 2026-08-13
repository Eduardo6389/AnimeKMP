package com.jetbrains.kmpapp.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.jetbrains.kmpapp.domain.model.ItemDetalle
import com.jetbrains.kmpapp.presentation.DetalleViewModel
import com.jetbrains.kmpapp.ui.components.ErrorView
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleScreen(
    id: String,
    onBack: () -> Unit,
    viewModel: DetalleViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(id) { viewModel.cargar(id) }
    Scaffold(topBar = { BarraDetalle(onBack) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            val error = state.error
            when {
                state.cargando -> CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
                error != null -> ErrorView(error, viewModel::reintentar)
                state.detalle != null ->
                    state.detalle?.let {
                        DetalleContenido(it, state.esFavorito, viewModel::alternarFavorito)
                    }
                else -> Text("Sin datos")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BarraDetalle(onBack: () -> Unit) {
    TopAppBar(
        title = { Text("Detalle") },
        navigationIcon = { Button(onClick = onBack) { Text("Atrás") } },
    )
}

@Composable
private fun DetalleContenido(
    detalle: ItemDetalle,
    favorito: Boolean,
    onFavorito: () -> Unit,
) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        ImagenDetalle(detalle)
        EncabezadoDetalle(detalle, favorito, onFavorito)
        detalle.atributos.forEach { Text("${it.etiqueta}: ${it.valor}") }
        Text(detalle.descripcion.ifBlank { "Sin descripción" })
    }
}

@Composable
private fun ImagenDetalle(detalle: ItemDetalle) {
    AsyncImage(
        model = detalle.item.imagenUrl,
        contentDescription = "Portada grande de ${detalle.item.titulo}",
        modifier = Modifier.fillMaxWidth().size(300.dp),
        contentScale = ContentScale.Fit,
    )
}

@Composable
private fun EncabezadoDetalle(
    detalle: ItemDetalle,
    favorito: Boolean,
    onFavorito: () -> Unit,
) {
    Row(Modifier.fillMaxWidth()) {
        Text(detalle.item.titulo, Modifier.weight(1f), fontWeight = FontWeight.Bold)
        Button(onClick = onFavorito) { Text(if (favorito) "★" else "☆") }
    }
}
