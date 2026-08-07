package com.jetbrains.kmpapp.screens.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jetbrains.kmpapp.presentation.list.MovieListViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * Pantalla de LISTA (versión Sesión 5: funcional y simple).
 * Observa el `state` del ViewModel y dibuja según el estado. En la Sesión 6 la enriquecemos con
 * pósters, pull-to-refresh y un estado de error con botón "Reintentar".
 */
@Composable
fun ListScreen(
    onMovieClick: (movieId: Int) -> Unit,
) {
    // koinViewModel: Koin nos entrega el ViewModel ya armado con sus dependencias.
    val viewModel = koinViewModel<MovieListViewModel>()
    // collectAsStateWithLifecycle: observa el StateFlow respetando el ciclo de vida de la pantalla.
    val state by viewModel.state.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        when {
            // 1) Cargando inicial (aún sin datos).
            state.cargando -> CircularProgressIndicator(Modifier.align(Alignment.Center))

            // 2) Error y sin datos que mostrar.
            state.error != null -> Text(state.error!!, Modifier.align(Alignment.Center))

            // 3) Vacío (terminó de cargar pero no hay resultados).
            state.peliculas.isEmpty() -> Text("Sin resultados", Modifier.align(Alignment.Center))

            // 4) Datos.
            else -> LazyColumn(Modifier.fillMaxSize()) {
                items(state.peliculas, key = { it.id }) { pelicula ->
                    ListItem(
                        headlineContent = { Text(pelicula.titulo) },
                        supportingContent = { Text("${pelicula.anio}  ·  ★ ${pelicula.rating}") },
                        modifier = Modifier.clickable { onMovieClick(pelicula.id) },
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}
