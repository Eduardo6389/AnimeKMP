package com.jetbrains.kmpapp.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jetbrains.kmpapp.domain.model.Tema
import com.jetbrains.kmpapp.presentation.AjustesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjustesScreen(
    viewModel: AjustesViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    Scaffold(topBar = {
        TopAppBar(title = { Text("Ajustes") }, navigationIcon = {
            Button(onClick = onBack) { Text("Atrás") }
        })
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("Tema")
            Tema.entries.forEach { tema ->
                FilaTema(tema, state.tema == tema) { viewModel.cambiarTema(tema) }
            }
            Button(onClick = viewModel::limpiarCache, enabled = !state.limpiando) {
                Text(if (state.limpiando) "Borrando..." else "Borrar caché")
            }
        }
    }
}

@Composable
private fun FilaTema(
    tema: Tema,
    seleccionado: Boolean,
    onClick: () -> Unit,
) {
    Row {
        RadioButton(selected = seleccionado, onClick = onClick)
        Text(tema.name.lowercase().replaceFirstChar { it.uppercase() }, Modifier.padding(top = 12.dp))
    }
}
