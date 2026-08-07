package com.jetbrains.kmpapp.di

import com.jetbrains.kmpapp.data.local.MovieLocalDataSource
import com.jetbrains.kmpapp.data.local.SqlDelightMovieLocalDataSource
import com.jetbrains.kmpapp.data.remote.TmdbApi
import com.jetbrains.kmpapp.data.remote.crearTmdbHttpClient
import com.jetbrains.kmpapp.data.repository.MovieRepositoryImpl
import com.jetbrains.kmpapp.domain.repository.MovieRepository
import com.jetbrains.kmpapp.domain.usecase.BuscarPeliculas
import com.jetbrains.kmpapp.domain.usecase.GetDetalle
import com.jetbrains.kmpapp.domain.usecase.GetFavoritas
import com.jetbrains.kmpapp.domain.usecase.GetPopulares
import com.jetbrains.kmpapp.domain.usecase.MarcarFavorita
import com.jetbrains.kmpapp.presentation.detail.MovieDetailViewModel
import com.jetbrains.kmpapp.presentation.favorites.FavoritesViewModel
import com.jetbrains.kmpapp.presentation.list.MovieListViewModel
import org.koin.core.KoinApplication
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * INYECCIÓN DE DEPENDENCIAS con Koin. En vez de crear objetos a mano ("new"), los registramos
 * aquí y Koin los arma y conecta por nosotros. `get()` resuelve una dependencia por su tipo.
 *
 * Capas (Clean Architecture):
 *   data → red (TmdbApi) + local (DataSource) + repositorio
 *   domain → casos de uso (dependen de la interfaz MovieRepository, no de la implementación)
 *   presentation → ViewModels
 *
 * OJO: aquí NO se crea la base de datos (CineDb). Ese objeto necesita cosas de plataforma
 * (Context en Android), así que lo aporta el `platformModule` de cada plataforma (ver
 * KoinAndroid.kt / KoinIos.kt).
 */
val dataModule = module {
    // Red
    single { crearTmdbHttpClient() }          // usa CineBuildConfig.TMDB_API_KEY (de local.properties)
    singleOf(::TmdbApi)

    // Local (el CineDb lo provee el módulo de plataforma)
    single<MovieLocalDataSource> { SqlDelightMovieLocalDataSource(get()) }

    // Repositorio (une red + local); se expone como la interfaz del dominio.
    singleOf(::MovieRepositoryImpl) bind MovieRepository::class
}

val domainModule = module {
    factoryOf(::GetPopulares)
    factoryOf(::GetFavoritas)
    factoryOf(::BuscarPeliculas)
    factoryOf(::GetDetalle)
    factoryOf(::MarcarFavorita)
}

val viewModelModule = module {
    // Los ViewModels se registran como factory y se piden con koinViewModel<...>() en Compose.
    factoryOf(::MovieListViewModel)
    factoryOf(::MovieDetailViewModel)
    factoryOf(::FavoritesViewModel)
}

/**
 * Arranca Koin con todos los módulos comunes + el módulo específico de la plataforma
 * (que aporta el CineDb con su driver de SQLite).
 */
fun initKoin(platformModule: Module): KoinApplication =
    org.koin.core.context.startKoin {
        modules(platformModule, dataModule, domainModule, viewModelModule)
    }
