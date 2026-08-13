package com.jetbrains.kmpapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jetbrains.kmpapp.domain.model.Item
import com.jetbrains.kmpapp.presentation.ListaState
import com.jetbrains.kmpapp.presentation.ListaViewModel
import com.jetbrains.kmpapp.ui.components.ErrorView
import com.jetbrains.kmpapp.ui.components.ItemRow
import com.jetbrains.kmpapp.ui.components.ItemRowAcciones
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaScreen(
    navegacion: ListaNavegacion,
    viewModel: ListaViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    Scaffold(topBar = { BarraPrincipal(navegacion) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Busqueda(state.query, viewModel::onQueryChange)
            Ordenes(viewModel)
            if (state.sinConexion) AvisoSinConexion()
            ContenidoLista(state, viewModel, navegacion.detalle)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BarraPrincipal(navegacion: ListaNavegacion) {
    TopAppBar(
        title = { Text("AnimeKMP") },
        actions = {
            Button(onClick = navegacion.favoritos) { Text("Favoritos") }
            Button(onClick = navegacion.ajustes) { Text("Ajustes") }
        },
    )
}

@Composable
private fun Busqueda(
    query: String,
    onChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onChange,
        label = { Text("Buscar anime") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
    )
}

@Composable
private fun Ordenes(viewModel: ListaViewModel) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        Button(onClick = viewModel::ordenarMetrica) { Text("Puntuación") }
        Button(onClick = viewModel::ordenarTitulo) { Text("Título") }
        Button(onClick = viewModel::ordenarFecha) { Text("Fecha") }
    }
}

@Composable
private fun AvisoSinConexion() {
    Text(
        text = "Sin conexión: mostrando datos guardados",
        modifier = Modifier.fillMaxWidth().padding(8.dp),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContenidoLista(
    state: ListaState,
    viewModel: ListaViewModel,
    onDetalle: (String) -> Unit,
) {
    val error = state.error
    when {
        state.cargando && state.items.isEmpty() -> Cargando()
        error != null -> ErrorView(error, viewModel::reintentar)
        state.vacio -> Vacio()
        else ->
            PullToRefreshBox(
                isRefreshing = state.cargando,
                onRefresh = viewModel::refrescar,
                modifier = Modifier.fillMaxSize(),
            ) { Lista(state, viewModel, onDetalle) }
    }
}

@Composable
private fun Lista(
    state: ListaState,
    viewModel: ListaViewModel,
    onDetalle: (String) -> Unit,
) {
    LazyColumn(Modifier.fillMaxSize()) {
        itemsIndexed(state.items, key = { _, item -> item.id }) { index, item ->
            FilaLista(item, state, viewModel, onDetalle)
            if (index == state.items.lastIndex) {
                LaunchedEffect(state.items.size) { viewModel.cargarSiguientePagina() }
            }
            HorizontalDivider()
        }
        if (state.cargandoMas) item { Cargando() }
        if (!state.hayMas) item { Text("No hay más", Modifier.padding(16.dp)) }
    }
}

@Composable
private fun FilaLista(
    item: Item,
    state: ListaState,
    viewModel: ListaViewModel,
    onDetalle: (String) -> Unit,
) {
    ItemRow(
        item = item,
        favorito = state.favoritos.contains(item.id),
        acciones =
            ItemRowAcciones(
                abrir = { onDetalle(item.id) },
                favorito = { viewModel.alternarFavorito(item.id) },
            ),
    )
}

@Composable
private fun Cargando() {
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) { CircularProgressIndicator() }
}

@Composable
private fun Vacio() {
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) { Text("No hay resultados") }
}
