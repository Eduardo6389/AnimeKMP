package com.jetbrains.kmpapp.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jetbrains.kmpapp.domain.model.Movie
import com.jetbrains.kmpapp.domain.usecase.GetPopulares
import com.jetbrains.kmpapp.presentation.mensajeAmigable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Estado de la pantalla de LISTA. La UI dibuja EXACTAMENTE esto (patrón UDF: estado abajo).
 * Modela los 4 casos que la pantalla debe cubrir (Sesión 6):
 *   cargando (inicial) · error · vacío (sin resultados) · datos.
 */
data class MovieListState(
    val cargando: Boolean = false,      // carga inicial: aún no hay datos y estamos bajando
    val refrescando: Boolean = false,   // pull-to-refresh: ya hay datos y estamos re-bajando
    val peliculas: List<Movie> = emptyList(),
    val error: String? = null,
)

/**
 * ViewModel COMPARTIDO (Android e iOS lo reutilizan) de la lista de populares.
 *
 * Fuentes de estado que combinamos:
 *  - getPopulares()  → Flow de la DB (offline-first): la fuente de verdad de la lista.
 *  - _refrescando    → si hay una descarga de red en curso.
 *  - _error          → último error de red (para mostrar y poder "reintentar").
 */
class MovieListViewModel(
    private val getPopulares: GetPopulares,
) : ViewModel() {

    private val _refrescando = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    val state: StateFlow<MovieListState> =
        combine(getPopulares(), _refrescando, _error) { peliculas, refrescando, error ->
            MovieListState(
                cargando = refrescando && peliculas.isEmpty(),
                refrescando = refrescando,
                peliculas = peliculas,
                // Si ya tenemos datos en la DB, no tapamos la pantalla con el error de red.
                error = if (peliculas.isEmpty()) error else null,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MovieListState(cargando = true),
        )

    init {
        refrescar() // al crear la pantalla, intentamos traer datos frescos
    }

    /** Baja populares de la red y las guarda en la DB. Se usa al iniciar y en pull-to-refresh. */
    fun refrescar() {
        viewModelScope.launch {
            _refrescando.value = true
            _error.value = null
            try {
                getPopulares.refrescar()
            } catch (e: Exception) {
                _error.value = e.mensajeAmigable()
            } finally {
                _refrescando.value = false
            }
        }
    }
}
