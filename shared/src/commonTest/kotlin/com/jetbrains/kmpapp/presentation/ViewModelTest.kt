package com.jetbrains.kmpapp.presentation

import com.jetbrains.kmpapp.domain.model.AppError
import com.jetbrains.kmpapp.domain.model.ItemDetalle
import com.jetbrains.kmpapp.domain.model.Resultado
import com.jetbrains.kmpapp.domain.usecase.BuscarItems
import com.jetbrains.kmpapp.domain.usecase.GetFavoritos
import com.jetbrains.kmpapp.domain.usecase.GetItems
import com.jetbrains.kmpapp.domain.usecase.RefrescarCatalogo
import com.jetbrains.kmpapp.domain.usecase.ToggleFavorito
import com.jetbrains.kmpapp.test.FakeConectividad
import com.jetbrains.kmpapp.test.FakeItemRepository
import com.jetbrains.kmpapp.test.item
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun preparar() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun cerrar() {
        Dispatchers.resetMain()
    }

    @Test
    fun listaCargaPaginaInicial() =
        runTest(dispatcher) {
            val (vm, repo) = lista()
            advanceUntilIdle()
            assertEquals(1, repo.paginaPedida)
            vm.clearForIos()
        }

    @Test
    fun listaPublicaDatosDelCatalogo() =
        runTest(dispatcher) {
            val (vm, repo) = lista()
            repo.catalogo.value = listOf(item(titulo = "Naruto"))
            advanceUntilIdle()
            assertEquals(
                "Naruto",
                vm.state.value.items
                    .first()
                    .titulo,
            )
            vm.clearForIos()
        }

    @Test
    fun listaMuestraErrorInicial() =
        runTest(dispatcher) {
            val repo =
                FakeItemRepository().apply {
                    cargarResultado = Resultado.Fallo(AppError.Timeout)
                }
            val vm = lista(repo).first
            advanceUntilIdle()
            assertIs<AppError.Timeout>(vm.state.value.error)
            vm.clearForIos()
        }

    @Test
    fun busquedaVaciaProduceEstadoVacio() =
        runTest(dispatcher) {
            val (vm, repo) = lista()
            repo.buscarResultado = Resultado.Ok(emptyList())
            vm.onQueryChange("sin resultados")
            advanceTimeBy(301)
            advanceUntilIdle()
            assertTrue(vm.state.value.vacio)
            vm.clearForIos()
        }

    @Test
    fun debounceHaceUnaSolaBusqueda() =
        runTest(dispatcher) {
            val (vm, repo) = lista()
            runCurrent()
            vm.onQueryChange("n")
            vm.onQueryChange("na")
            vm.onQueryChange("naruto")
            advanceTimeBy(301)
            advanceUntilIdle()
            assertEquals(1, repo.busquedas)
            assertEquals("naruto", repo.ultimoQuery)
            vm.clearForIos()
        }

    @Test
    fun paginacionPidePaginaDos() =
        runTest(dispatcher) {
            val (vm, repo) = lista()
            repo.cargarResultado = Resultado.Ok(true)
            advanceUntilIdle()
            vm.cargarSiguientePagina()
            advanceUntilIdle()
            assertEquals(2, repo.paginaPedida)
            vm.clearForIos()
        }

    @Test
    fun favoritoSeAlternaDesdeLista() =
        runTest(dispatcher) {
            val (vm, repo) = lista()
            vm.alternarFavorito("1")
            advanceUntilIdle()
            assertEquals(1, repo.favoritosCambios)
            vm.clearForIos()
        }

    @Test
    fun conectividadActualizaEstado() =
        runTest(dispatcher) {
            val conectividad = FakeConectividad(true)
            val vm = lista(conectividad = conectividad).first
            conectividad.cambiar(false)
            advanceUntilIdle()
            assertTrue(vm.state.value.sinConexion)
            vm.clearForIos()
        }

    @Test
    fun detallePublicaRespuesta() =
        runTest(dispatcher) {
            val repo =
                FakeItemRepository().apply {
                    detalleResultado =
                        Resultado.Ok(
                            ItemDetalle(item(), "Descripcion", emptyList(), emptyList()),
                        )
                }
            val vm =
                DetalleViewModel(
                    DetalleUseCases(
                        com.jetbrains.kmpapp.domain.usecase
                            .GetDetalle(repo),
                        ToggleFavorito(repo),
                        GetFavoritos(repo),
                    ),
                )
            vm.cargar("1")
            advanceUntilIdle()
            assertEquals(
                "Descripcion",
                vm.state.value.detalle
                    ?.descripcion,
            )
            assertFalse(vm.state.value.cargando)
            vm.clearForIos()
        }

    private fun lista(
        repo: FakeItemRepository = FakeItemRepository(),
        conectividad: FakeConectividad = FakeConectividad(),
    ): Pair<ListaViewModel, FakeItemRepository> {
        val casos =
            ListaUseCases(
                GetItems(repo),
                BuscarItems(repo),
                RefrescarCatalogo(repo),
                ToggleFavorito(repo),
                GetFavoritos(repo),
                conectividad,
            )
        return ListaViewModel(casos) to repo
    }
}
