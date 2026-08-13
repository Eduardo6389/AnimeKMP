package com.jetbrains.kmpapp.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.jetbrains.kmpapp.presentation.FavoritosState
import com.jetbrains.kmpapp.presentation.FavoritosViewModel
import com.jetbrains.kmpapp.ui.components.ErrorView
import com.jetbrains.kmpapp.ui.components.ItemRow
import com.jetbrains.kmpapp.ui.components.ItemRowAcciones
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritosScreen(
    onDetalle: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: FavoritosViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    Scaffold(topBar = { BarraFavoritos(onBack) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            ContenidoFavoritos(state, viewModel, onDetalle)
        }
    }
}

@Composable
private fun ContenidoFavoritos(
    state: FavoritosState,
    viewModel: FavoritosViewModel,
    onDetalle: (String) -> Unit,
) {
    val error = state.error
    when {
        state.cargando -> CircularProgressIndicator()
        error != null -> ErrorView(error, viewModel::reintentar)
        state.vacio -> Text("No tienes favoritos")
        else -> ListaFavoritos(state, viewModel, onDetalle)
    }
}

@Composable
private fun ListaFavoritos(
    state: FavoritosState,
    viewModel: FavoritosViewModel,
    onDetalle: (String) -> Unit,
) {
    LazyColumn {
        items(state.items, key = { it.id }) { item ->
            ItemRow(
                item,
                true,
                ItemRowAcciones(
                    abrir = { onDetalle(item.id) },
                    favorito = { viewModel.alternarFavorito(item.id) },
                ),
            )
            HorizontalDivider()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BarraFavoritos(onBack: () -> Unit) {
    TopAppBar(
        title = { Text("Favoritos") },
        navigationIcon = { Button(onClick = onBack) { Text("Atrás") } },
    )
}
