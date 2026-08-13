package com.jetbrains.kmpapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.jetbrains.kmpapp.domain.model.Item

@Composable
fun ItemRow(
    item: Item,
    favorito: Boolean,
    acciones: ItemRowAcciones,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = acciones.abrir)
                .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Portada(item)
        Spacer(Modifier.width(12.dp))
        Datos(item, Modifier.weight(1f))
        Button(onClick = acciones.favorito) { Text(if (favorito) "★" else "☆") }
    }
}

@Composable
private fun Portada(item: Item) {
    AsyncImage(
        model = item.imagenUrl,
        contentDescription = "Portada de ${item.titulo}",
        modifier = Modifier.size(width = 78.dp, height = 110.dp),
        contentScale = ContentScale.Crop,
    )
}

@Composable
private fun Datos(
    item: Item,
    modifier: Modifier,
) {
    Column(modifier) {
        Text(item.titulo, fontWeight = FontWeight.Bold)
        item.subtitulo?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
        item.metrica?.let { Text("Puntuación: $it") }
        item.fecha?.let { Text("Año: $it") }
        Text(item.tags.joinToString(), style = MaterialTheme.typography.bodySmall)
    }
}
