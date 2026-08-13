package com.jetbrains.kmpapp.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jetbrains.kmpapp.domain.model.Tema
import com.jetbrains.kmpapp.presentation.AjustesViewModel
import com.jetbrains.kmpapp.ui.screens.AjustesScreen
import com.jetbrains.kmpapp.ui.screens.DetalleScreen
import com.jetbrains.kmpapp.ui.screens.FavoritosScreen
import com.jetbrains.kmpapp.ui.screens.ListaNavegacion
import com.jetbrains.kmpapp.ui.screens.ListaScreen
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AnimeKmpApp() {
    val ajustes: AjustesViewModel = koinViewModel()
    val state by ajustes.state.collectAsState()
    val oscuro =
        when (state.tema) {
            Tema.OSCURO -> true
            Tema.CLARO -> false
            Tema.SISTEMA -> isSystemInDarkTheme()
        }
    MaterialTheme(colorScheme = if (oscuro) darkColorScheme() else lightColorScheme()) {
        Navegacion(ajustes)
    }
}

@Composable
private fun Navegacion(ajustes: AjustesViewModel) {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = RUTA_LISTA) {
        lista(nav)
        detalle(nav)
        favoritos(nav)
        ajustes(nav, ajustes)
    }
}

private fun NavGraphBuilder.lista(nav: NavHostController) {
    composable(RUTA_LISTA) {
        ListaScreen(
            ListaNavegacion(
                detalle = { nav.navigate("detalle/$it") },
                favoritos = { nav.navigate(RUTA_FAVORITOS) },
                ajustes = { nav.navigate(RUTA_AJUSTES) },
            ),
        )
    }
}

private fun NavGraphBuilder.detalle(nav: NavHostController) {
    composable("detalle/{id}") { entrada ->
        DetalleScreen(
            id = entrada.arguments?.getString("id").orEmpty(),
            onBack = nav::popBackStack,
        )
    }
}

private fun NavGraphBuilder.favoritos(nav: NavHostController) {
    composable(RUTA_FAVORITOS) {
        FavoritosScreen(
            onDetalle = { nav.navigate("detalle/$it") },
            onBack = nav::popBackStack,
        )
    }
}

private fun NavGraphBuilder.ajustes(
    nav: NavHostController,
    vm: AjustesViewModel,
) {
    composable(RUTA_AJUSTES) {
        AjustesScreen(viewModel = vm, onBack = nav::popBackStack)
    }
}

private const val RUTA_LISTA = "lista"
private const val RUTA_FAVORITOS = "favoritos"
private const val RUTA_AJUSTES = "ajustes"
