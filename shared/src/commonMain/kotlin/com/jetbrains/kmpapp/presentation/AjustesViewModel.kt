package com.jetbrains.kmpapp.presentation

import androidx.lifecycle.viewModelScope
import com.jetbrains.kmpapp.domain.model.Tema
import com.jetbrains.kmpapp.domain.repository.AjustesRepository
import com.jetbrains.kmpapp.domain.repository.ItemRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AjustesState(
    val tema: Tema = Tema.SISTEMA,
    val limpiando: Boolean = false,
)

class AjustesViewModel(
    private val ajustes: AjustesRepository,
    private val items: ItemRepository,
) : SharedViewModel() {
    private val _state = MutableStateFlow(AjustesState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            ajustes.tema.collect { tema ->
                _state.update { it.copy(tema = tema) }
            }
        }
    }

    fun estadoActual(): AjustesState = state.value

    fun observar(onChange: (AjustesState) -> Unit): Observador = observar(state, onChange)

    fun cambiarTema(tema: Tema) {
        ajustes.cambiarTema(tema)
    }

    fun cambiarTema(nombre: String) {
        val tema = Tema.entries.firstOrNull { it.name == nombre } ?: Tema.SISTEMA
        cambiarTema(tema)
    }

    fun limpiarCache() {
        viewModelScope.launch {
            _state.update { it.copy(limpiando = true) }
            items.limpiarCache()
            _state.update { it.copy(limpiando = false) }
        }
    }
}
