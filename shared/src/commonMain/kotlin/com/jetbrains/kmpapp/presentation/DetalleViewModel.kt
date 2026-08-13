package com.jetbrains.kmpapp.presentation

import androidx.lifecycle.viewModelScope
import com.jetbrains.kmpapp.domain.model.AppError
import com.jetbrains.kmpapp.domain.model.ItemDetalle
import com.jetbrains.kmpapp.domain.model.Resultado
import com.jetbrains.kmpapp.domain.usecase.GetDetalle
import com.jetbrains.kmpapp.domain.usecase.GetFavoritos
import com.jetbrains.kmpapp.domain.usecase.ToggleFavorito
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DetalleUseCases(
    val getDetalle: GetDetalle,
    val toggleFavorito: ToggleFavorito,
    val getFavoritos: GetFavoritos,
)

data class DetalleState(
    val cargando: Boolean = false,
    val detalle: ItemDetalle? = null,
    val error: AppError? = null,
    val esFavorito: Boolean = false,
)

class DetalleViewModel(
    private val casos: DetalleUseCases,
) : SharedViewModel() {
    private val _state = MutableStateFlow(DetalleState())
    val state = _state.asStateFlow()
    private var idActual: String? = null

    init {
        observarFavoritos()
    }

    fun estadoActual(): DetalleState = state.value

    fun observar(onChange: (DetalleState) -> Unit): Observador = observar(state, onChange)

    fun cargar(id: String) {
        if (id == idActual && state.value.detalle != null) return
        idActual = id
        viewModelScope.launch {
            _state.value = DetalleState(cargando = true)
            when (val resultado = casos.getDetalle(id)) {
                is Resultado.Ok -> {
                    _state.update {
                        it.copy(cargando = false, detalle = resultado.valor)
                    }
                }
                is Resultado.Fallo -> {
                    _state.update {
                        it.copy(cargando = false, error = resultado.error)
                    }
                }
            }
        }
    }

    fun reintentar() {
        idActual?.let { id ->
            idActual = null
            cargar(id)
        }
    }

    fun mensajeError(): String? = state.value.error?.let(::mensajeAppError)

    fun alternarFavorito() {
        val id = idActual ?: return
        viewModelScope.launch {
            casos.toggleFavorito(id)
        }
    }

    private fun observarFavoritos() {
        viewModelScope.launch {
            casos.getFavoritos().collect { favoritos ->
                val esFavorito = favoritos.any { it.id == idActual }
                _state.update { it.copy(esFavorito = esFavorito) }
            }
        }
    }
}
