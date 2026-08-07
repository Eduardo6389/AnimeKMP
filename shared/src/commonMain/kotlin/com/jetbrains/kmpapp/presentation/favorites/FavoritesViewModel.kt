package com.jetbrains.kmpapp.presentation.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jetbrains.kmpapp.domain.model.Movie
import com.jetbrains.kmpapp.domain.usecase.GetFavoritas
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** Estado de la pantalla de FAVORITAS. */
data class FavoritesState(
    val favoritas: List<Movie> = emptyList(),
)

/**
 * ViewModel de Favoritas. Solo observa la DB (no toca la red): cuando marcas/desmarcas un
 * corazón en cualquier pantalla, esta lista se actualiza sola gracias al Flow de SQLDelight.
 */
class FavoritesViewModel(
    getFavoritas: GetFavoritas,
) : ViewModel() {

    val state: StateFlow<FavoritesState> =
        getFavoritas()
            .map { FavoritesState(favoritas = it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = FavoritesState(),
            )
}
