package com.jetbrains.kmpapp.presentation

import com.jetbrains.kmpapp.domain.model.MovieDetail
import com.jetbrains.kmpapp.domain.usecase.GetDetalle
import com.jetbrains.kmpapp.domain.usecase.GetFavoritas
import com.jetbrains.kmpapp.domain.usecase.GetPopulares
import com.jetbrains.kmpapp.domain.usecase.MarcarFavorita
import com.jetbrains.kmpapp.presentation.detail.MovieDetailViewModel
import com.jetbrains.kmpapp.presentation.favorites.FavoritesViewModel
import com.jetbrains.kmpapp.presentation.list.MovieListViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests de los ViewModels con un FakeMovieRepository (sin red ni DB reales).
 *
 * Detalle técnico: los ViewModels usan `viewModelScope` (Dispatchers.Main) y `stateIn(WhileSubscribed)`,
 * que es "frío" hasta que alguien observa. Por eso en cada test:
 *   1) fijamos un dispatcher de prueba como Main (setMain),
 *   2) lanzamos un colector del `state` para "activarlo",
 *   3) avanzamos el reloj virtual (advanceUntilIdle) y comprobamos `state.value`.
 */
class ViewModelsTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest fun antes() = Dispatchers.setMain(testDispatcher)
    @AfterTest fun despues() = Dispatchers.resetMain()

    // Mantiene vivo el StateFlow durante el test (simula que la pantalla lo observa).
    private fun kotlinx.coroutines.test.TestScope.activar(state: StateFlow<*>) =
        launch { state.collect { } }

    // ── MovieListViewModel ────────────────────────────────────────────────────

    @Test
    fun lista_al_iniciar_refresca_y_muestra_populares() = runTest {
        val repo = FakeMovieRepository().apply { enServidor = listOf(peli(1), peli(2)) }
        val vm = MovieListViewModel(GetPopulares(repo))
        val job = activar(vm.state)

        advanceUntilIdle()
        assertEquals(2, vm.state.value.peliculas.size)
        assertTrue(!vm.state.value.cargando)
        assertNull(vm.state.value.error)
        job.cancel()
    }

    @Test
    fun lista_muestra_error_si_la_red_falla_y_no_hay_datos() = runTest {
        val repo = FakeMovieRepository().apply { fallarAlRefrescar = true }
        val vm = MovieListViewModel(GetPopulares(repo))
        val job = activar(vm.state)

        advanceUntilIdle()
        assertNotNull(vm.state.value.error)
        assertTrue(vm.state.value.peliculas.isEmpty())
        job.cancel()
    }

    @Test
    fun lista_con_datos_no_tapa_con_error_de_red() = runTest {
        // Ya hay una película "cacheada" en la DB; aunque la red falle, se siguen viendo datos.
        val repo = FakeMovieRepository(peliculasEnDb = listOf(peli(7))).apply { fallarAlRefrescar = true }
        val vm = MovieListViewModel(GetPopulares(repo))
        val job = activar(vm.state)

        advanceUntilIdle()
        assertEquals(1, vm.state.value.peliculas.size)
        assertNull(vm.state.value.error) // hay datos → no mostramos el error a pantalla completa
        job.cancel()
    }

    @Test
    fun lista_refrescar_manual_recarga() = runTest {
        val repo = FakeMovieRepository()
        val vm = MovieListViewModel(GetPopulares(repo))
        val job = activar(vm.state)
        advanceUntilIdle()
        assertTrue(vm.state.value.peliculas.isEmpty())

        repo.enServidor = listOf(peli(1), peli(2), peli(3))
        vm.refrescar()
        advanceUntilIdle()
        assertEquals(3, vm.state.value.peliculas.size)
        job.cancel()
    }

    // ── MovieDetailViewModel ──────────────────────────────────────────────────

    @Test
    fun detalle_carga_pelicula_desde_la_db() = runTest {
        val repo = FakeMovieRepository(peliculasEnDb = listOf(peli(5, "Dune")))
        val vm = MovieDetailViewModel(GetDetalle(repo), MarcarFavorita(repo))
        val job = activar(vm.state)

        vm.cargar(5)
        advanceUntilIdle()
        assertEquals("Dune", vm.state.value.pelicula?.titulo)
        job.cancel()
    }

    @Test
    fun detalle_trae_datos_extra_de_la_red() = runTest {
        val repo = FakeMovieRepository(peliculasEnDb = listOf(peli(5))).apply {
            detalleADevolver = MovieDetail(5, "Dune", "", 8.4, null, null, "2021", 155, listOf("Sci-Fi"), null)
        }
        val vm = MovieDetailViewModel(GetDetalle(repo), MarcarFavorita(repo))
        val job = activar(vm.state)

        vm.cargar(5)
        advanceUntilIdle()
        assertEquals(155, vm.state.value.detalle?.duracionMin)
        assertEquals(listOf("Sci-Fi"), vm.state.value.detalle?.generos)
        job.cancel()
    }

    @Test
    fun detalle_alternar_favorito_marca_la_pelicula() = runTest {
        val repo = FakeMovieRepository(peliculasEnDb = listOf(peli(5, favorita = false)))
        val vm = MovieDetailViewModel(GetDetalle(repo), MarcarFavorita(repo))
        val job = activar(vm.state)
        vm.cargar(5)
        advanceUntilIdle()
        assertTrue(vm.state.value.pelicula?.esFavorita == false)

        vm.alternarFavorito()
        advanceUntilIdle()
        assertTrue(vm.state.value.pelicula?.esFavorita == true)
        job.cancel()
    }

    // ── FavoritesViewModel ────────────────────────────────────────────────────

    @Test
    fun favoritas_muestra_solo_las_marcadas() = runTest {
        val repo = FakeMovieRepository(
            peliculasEnDb = listOf(peli(1, favorita = true), peli(2, favorita = false), peli(3, favorita = true)),
        )
        val vm = FavoritesViewModel(GetFavoritas(repo))
        val job = activar(vm.state)

        advanceUntilIdle()
        assertEquals(2, vm.state.value.favoritas.size)
        assertTrue(vm.state.value.favoritas.all { it.esFavorita })
        job.cancel()
    }

    @Test
    fun favoritas_reacciona_al_marcar_una_nueva() = runTest {
        val repo = FakeMovieRepository(peliculasEnDb = listOf(peli(1), peli(2)))
        val vm = FavoritesViewModel(GetFavoritas(repo))
        val job = activar(vm.state)
        advanceUntilIdle()
        assertTrue(vm.state.value.favoritas.isEmpty())

        repo.marcarFavorita(2, true)
        advanceUntilIdle()
        assertEquals(1, vm.state.value.favoritas.size)
        assertEquals(2, vm.state.value.favoritas.first().id)
        job.cancel()
    }
}
