package com.jetbrains.kmpapp.presentation

import androidx.lifecycle.viewModelScope
import com.jetbrains.kmpapp.domain.model.AppError
import com.jetbrains.kmpapp.domain.model.Item
import com.jetbrains.kmpapp.domain.model.Orden
import com.jetbrains.kmpapp.domain.model.Resultado
import com.jetbrains.kmpapp.domain.usecase.BuscarItems
import com.jetbrains.kmpapp.domain.usecase.GetFavoritos
import com.jetbrains.kmpapp.domain.usecase.GetItems
import com.jetbrains.kmpapp.domain.usecase.RefrescarCatalogo
import com.jetbrains.kmpapp.domain.usecase.ToggleFavorito
import com.jetbrains.kmpapp.platform.Conectividad
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ListaUseCases(
    val getItems: GetItems,
    val buscar: BuscarItems,
    val refrescar: RefrescarCatalogo,
    val toggleFavorito: ToggleFavorito,
    val getFavoritos: GetFavoritos,
    val conectividad: Conectividad,
)

data class ListaState(
    val cargando: Boolean = false,
    val cargandoMas: Boolean = false,
    val items: List<Item> = emptyList(),
    val query: String = "",
    val error: AppError? = null,
    val hayMas: Boolean = true,
    val sinConexion: Boolean = false,
    val orden: Orden = Orden.METRICA_DESC,
    val favoritos: Set<String> = emptySet(),
) {
    val vacio: Boolean
        get() = !cargando && items.isEmpty() && error == null
}

@OptIn(FlowPreview::class)
class ListaViewModel(
    private val casos: ListaUseCases,
) : SharedViewModel() {
    private val _state = MutableStateFlow(ListaState(cargando = true))
    val state = _state.asStateFlow()

    private var pagina = PAGINA_INICIAL
    private val queryFlow = MutableStateFlow("")
    private var catalogoJob: Job? = null

    init {
        observarCatalogo()
        observarFavoritos()
        observarConectividad()
        observarBusqueda()
        refrescarInicial()
    }

    fun estadoActual(): ListaState = state.value

    fun observar(onChange: (ListaState) -> Unit): Observador = observar(state, onChange)

    fun onQueryChange(query: String) {
        _state.update { it.copy(query = query, error = null) }
        queryFlow.value = query
    }

    fun cargarSiguientePagina() {
        val actual = state.value
        if (actual.query.isNotBlank() || actual.cargandoMas || !actual.hayMas) return
        viewModelScope.launch {
            _state.update { it.copy(cargandoMas = true) }
            aplicarPagina(casos.getItems.cargarPagina(pagina + 1), pagina + 1)
        }
    }

    fun refrescar() {
        pagina = PAGINA_INICIAL
        viewModelScope.launch {
            _state.update { it.copy(cargando = true, error = null) }
            aplicarPagina(casos.refrescar(forzar = true), PAGINA_INICIAL)
        }
    }

    fun reintentar() {
        if (state.value.query.isBlank()) refrescar() else buscarActual()
    }

    fun alternarFavorito(id: String) {
        viewModelScope.launch {
            casos.toggleFavorito(id)
        }
    }

    fun cambiarOrden(orden: Orden) {
        if (orden == state.value.orden) return
        _state.update { it.copy(orden = orden) }
        observarCatalogo()
    }

    fun ordenarMetrica() = cambiarOrden(Orden.METRICA_DESC)

    fun ordenarTitulo() = cambiarOrden(Orden.TITULO_ASC)

    fun ordenarFecha() = cambiarOrden(Orden.FECHA_DESC)

    fun esFavorito(id: String): Boolean = state.value.favoritos.contains(id)

    fun mensajeError(): String? = state.value.error?.let(::mensajeAppError)

    private fun refrescarInicial() {
        viewModelScope.launch {
            aplicarPagina(casos.refrescar(), PAGINA_INICIAL)
        }
    }

    private fun observarBusqueda() {
        viewModelScope.launch {
            queryFlow
                .drop(1)
                .debounce(DEBOUNCE_MILLIS)
                .distinctUntilChanged()
                .collectLatest { query ->
                    if (query.isBlank()) observarCatalogo() else buscar(query)
                }
        }
    }

    private fun observarCatalogo() {
        catalogoJob?.cancel()
        catalogoJob =
            viewModelScope.launch {
                casos.getItems(state.value.orden).collect { items ->
                    if (state.value.query.isBlank()) {
                        _state.update {
                            it.copy(items = items, error = if (items.isEmpty()) it.error else null)
                        }
                    }
                }
            }
    }

    private fun observarFavoritos() {
        viewModelScope.launch {
            casos.getFavoritos().collect { favoritos ->
                _state.update {
                    it.copy(favoritos = favoritos.map(Item::id).toSet())
                }
            }
        }
    }

    private fun observarConectividad() {
        viewModelScope.launch {
            casos.conectividad.conectado.collect { conectado ->
                _state.update { it.copy(sinConexion = !conectado) }
            }
        }
    }

    private suspend fun buscar(query: String) {
        _state.update { it.copy(cargando = true, error = null) }
        when (val resultado = casos.buscar(query)) {
            is Resultado.Ok -> {
                _state.update {
                    it.copy(cargando = false, items = resultado.valor, error = null)
                }
            }
            is Resultado.Fallo -> {
                _state.update {
                    it.copy(cargando = false, items = emptyList(), error = resultado.error)
                }
            }
        }
    }

    private fun aplicarPagina(
        resultado: Resultado<Boolean>,
        nuevaPagina: Int,
    ) {
        when (resultado) {
            is Resultado.Ok -> {
                pagina = nuevaPagina
                _state.update {
                    it.copy(
                        cargando = false,
                        cargandoMas = false,
                        hayMas = resultado.valor,
                        error = null,
                    )
                }
            }
            is Resultado.Fallo -> aplicarError(resultado.error)
        }
    }

    private fun aplicarError(error: AppError) {
        _state.update {
            it.copy(
                cargando = false,
                cargandoMas = false,
                error = if (it.items.isEmpty()) error else null,
                sinConexion = it.sinConexion || error is AppError.SinConexion,
            )
        }
    }

    private fun buscarActual() {
        viewModelScope.launch {
            buscar(state.value.query)
        }
    }

    companion object {
        private const val PAGINA_INICIAL = 1
        private const val DEBOUNCE_MILLIS = 300L
    }
}
