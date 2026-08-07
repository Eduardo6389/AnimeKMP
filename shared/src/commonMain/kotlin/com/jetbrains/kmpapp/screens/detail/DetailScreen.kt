package com.jetbrains.kmpapp.screens.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jetbrains.kmpapp.presentation.detail.MovieDetailViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * Pantalla de DETALLE (versión Sesión 5: campos básicos).
 * En la Sesión 6 añadimos el póster grande y el botón de FAVORITO.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    movieId: Int,
    onBack: () -> Unit,
) {
    val viewModel = koinViewModel<MovieDetailViewModel>()
    // LaunchedEffect(movieId): al entrar (o si cambia el id), pedimos esa película.
    LaunchedEffect(movieId) { viewModel.cargar(movieId) }
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.pelicula?.titulo ?: "Detalle") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            val pelicula = state.pelicula
            when {
                state.cargando -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                pelicula == null -> Text(
                    state.error ?: "No se encontró la película",
                    Modifier.align(Alignment.Center),
                )
                else -> Column(
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(pelicula.titulo, style = MaterialTheme.typography.headlineSmall)
                    Text("${pelicula.anio}  ·  ★ ${pelicula.rating}", style = MaterialTheme.typography.bodyMedium)
                    // Datos extra de la red (géneros/duración), si ya llegaron.
                    state.detalle?.let { d ->
                        if (d.generos.isNotEmpty()) Text("Géneros: ${d.generos.joinToString()}")
                        d.duracionMin?.let { Text("Duración: $it min") }
                    }
                    Text(pelicula.overview, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}
