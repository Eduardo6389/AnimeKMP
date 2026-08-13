package com.jetbrains.kmpapp.data.local

import com.jetbrains.kmpapp.domain.model.Tema
import com.jetbrains.kmpapp.domain.repository.AjustesRepository
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AjustesRepositoryImpl(
    private val settings: Settings = Settings(),
) : AjustesRepository {
    private val _tema = MutableStateFlow(leerTema())
    override val tema = _tema.asStateFlow()

    override fun cambiarTema(tema: Tema) {
        settings.putString(CLAVE_TEMA, tema.name)
        _tema.value = tema
    }

    private fun leerTema(): Tema =
        Tema.entries.firstOrNull {
            it.name == settings.getString(CLAVE_TEMA, Tema.SISTEMA.name)
        } ?: Tema.SISTEMA
}

private const val CLAVE_TEMA = "tema"
