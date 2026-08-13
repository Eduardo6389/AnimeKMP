package com.jetbrains.kmpapp.presentation

import androidx.lifecycle.viewModelScope
import com.jetbrains.kmpapp.domain.model.AppError
import com.jetbrains.kmpapp.domain.model.Item
import com.jetbrains.kmpapp.domain.usecase.GetFavoritos
import com.jetbrains.kmpapp.domain.usecase.ToggleFavorito
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class FavoritosState(
    val cargando: Boolean = true,
    val items: List<Item> = emptyList(),
    val error: AppError? = null,
) {
    val vacio: Boolean
        get() = !cargando && items.isEmpty() && error == null
}

class FavoritosViewModel(
    private val getFavoritos: GetFavoritos,
    private val toggleFavorito: ToggleFavorito,
) : SharedViewModel() {
    private val _state = MutableStateFlow(FavoritosState())
    val state = _state.asStateFlow()
    private var observarJob: Job? = null

    init {
        observarFavoritos()
    }

    fun estadoActual(): FavoritosState = state.value

    fun observar(onChange: (FavoritosState) -> Unit): Observador = observar(state, onChange)

    fun alternarFavorito(id: String) {
        viewModelScope.launch {
            toggleFavorito(id)
        }
    }

    fun reintentar() {
        _state.value = FavoritosState(cargando = true)
        observarFavoritos()
    }

    fun mensajeError(): String? = state.value.error?.let(::mensajeAppError)

    private fun observarFavoritos() {
        observarJob?.cancel()
        observarJob =
            viewModelScope.launch {
                getFavoritos()
                    .catch { error -> mostrarError(error) }
                    .collect { items ->
                        _state.value = FavoritosState(cargando = false, items = items)
                    }
            }
    }

    private fun mostrarError(error: Throwable) {
        _state.value =
            FavoritosState(
                cargando = false,
                error = AppError.Desconocido(error.message.orEmpty()),
            )
    }
}
