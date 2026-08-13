package com.jetbrains.kmpapp.di

import com.jetbrains.kmpapp.data.local.AjustesRepositoryImpl
import com.jetbrains.kmpapp.data.local.ItemLocalDataSource
import com.jetbrains.kmpapp.data.local.SqlDelightItemLocalDataSource
import com.jetbrains.kmpapp.data.remote.ApiClient
import com.jetbrains.kmpapp.data.remote.crearHttpClient
import com.jetbrains.kmpapp.data.repository.ItemRepositoryImpl
import com.jetbrains.kmpapp.domain.repository.AjustesRepository
import com.jetbrains.kmpapp.domain.repository.ItemRepository
import com.jetbrains.kmpapp.domain.usecase.BuscarItems
import com.jetbrains.kmpapp.domain.usecase.GetDetalle
import com.jetbrains.kmpapp.domain.usecase.GetFavoritos
import com.jetbrains.kmpapp.domain.usecase.GetItems
import com.jetbrains.kmpapp.domain.usecase.RefrescarCatalogo
import com.jetbrains.kmpapp.domain.usecase.ToggleFavorito
import com.jetbrains.kmpapp.presentation.AjustesViewModel
import com.jetbrains.kmpapp.presentation.DetalleUseCases
import com.jetbrains.kmpapp.presentation.DetalleViewModel
import com.jetbrains.kmpapp.presentation.FavoritosViewModel
import com.jetbrains.kmpapp.presentation.ListaUseCases
import com.jetbrains.kmpapp.presentation.ListaViewModel
import org.koin.core.KoinApplication
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

private val dataModule =
    module {
        single { crearHttpClient() }
        single { ApiClient(get()) }
        single<ItemLocalDataSource> { SqlDelightItemLocalDataSource(get()) }
        single<AjustesRepository> { AjustesRepositoryImpl() }
        single<ItemRepository> { ItemRepositoryImpl(get(), get(), get()) }
    }

private val domainModule =
    module {
        factory { GetItems(get()) }
        factory { BuscarItems(get()) }
        factory { GetDetalle(get()) }
        factory { ToggleFavorito(get()) }
        factory { GetFavoritos(get()) }
        factory { RefrescarCatalogo(get()) }
    }

private val presentationModule =
    module {
        factory {
            ListaUseCases(get(), get(), get(), get(), get(), get())
        }
        factory { DetalleUseCases(get(), get(), get()) }
        viewModel { ListaViewModel(get()) }
        viewModel { DetalleViewModel(get()) }
        viewModel { FavoritosViewModel(get(), get()) }
        viewModel { AjustesViewModel(get(), get()) }
    }

fun initKoin(platformModule: Module): KoinApplication =
    org.koin.core.context.startKoin {
        modules(platformModule, dataModule, domainModule, presentationModule)
    }

class AppContainer : KoinComponent {
    fun listaViewModel(): ListaViewModel = ListaViewModel(get())

    fun detalleViewModel(): DetalleViewModel = DetalleViewModel(get())

    fun favoritosViewModel(): FavoritosViewModel = FavoritosViewModel(get(), get())

    fun ajustesViewModel(): AjustesViewModel = AjustesViewModel(get(), get())
}
