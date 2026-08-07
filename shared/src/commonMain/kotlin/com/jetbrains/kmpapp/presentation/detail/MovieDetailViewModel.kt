package com.jetbrains.kmpapp.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jetbrains.kmpapp.domain.model.Movie
import com.jetbrains.kmpapp.domain.model.MovieDetail
import com.jetbrains.kmpapp.domain.usecase.GetDetalle
import com.jetbrains.kmpapp.domain.usecase.MarcarFavorita
import com.jetbrains.kmpapp.presentation.mensajeAmigable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Estado de la pantalla de DETALLE. */
data class MovieDetailState(
    val cargando: Boolean = true,
    /** Datos base desde la DB (reactivos): incluyen el estado de FAVORITO en tiempo real. */
    val pelicula: Movie? = null,
    /** Datos extra desde la red (géneros, duración, tagline). Pueden llegar después. */
    val detalle: MovieDetail? = null,
    val error: String? = null,
)

/**
 * ViewModel del detalle. La pantalla llama a [cargar] con el id (en un LaunchedEffect).
 *
 * `flatMapLatest`: cuando cambia el id, cambiamos de flujo al de esa película en la DB.
 * Así el botón de favorito se refleja al instante (la DB emite → el estado se recalcula).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MovieDetailViewModel(
    private val getDetalle: GetDetalle,
    private val marcarFavorita: MarcarFavorita,
) : ViewModel() {

    private val _id = MutableStateFlow<Int?>(null)
    private val _detalle = MutableStateFlow<MovieDetail?>(null)
    private val _error = MutableStateFlow<String?>(null)

    val state: StateFlow<MovieDetailState> =
        combine(
            _id.flatMapLatest { id -> if (id == null) flowOf(null) else getDetalle.pelicula(id) },
            _detalle,
            _error,
        ) { pelicula, detalle, error ->
            MovieDetailState(
                cargando = pelicula == null && error == null,
                pelicula = pelicula,
                detalle = detalle,
                error = error,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MovieDetailState(),
        )

    /** Fija el id de la película y pide su detalle completo a la red (una sola vez por id). */
    fun cargar(id: Int) {
        if (_id.value == id) return
        _id.value = id
        _error.value = null
        viewModelScope.launch {
            try {
                _detalle.value = getDetalle(id)
            } catch (e: Exception) {
                // El detalle extra falló, pero los datos de la DB siguen mostrándose.
                _error.value = e.mensajeAmigable()
            }
        }
    }

    /** Alterna el favorito de la película actual (guarda en la DB). */
    fun alternarFavorito() {
        val actual = state.value.pelicula ?: return
        viewModelScope.launch {
            marcarFavorita(actual.id, !actual.esFavorita)
        }
    }
}
