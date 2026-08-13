package com.jetbrains.kmpapp.domain.repository

import com.jetbrains.kmpapp.domain.model.Tema
import kotlinx.coroutines.flow.StateFlow

interface AjustesRepository {
    val tema: StateFlow<Tema>

    fun cambiarTema(tema: Tema)
}
