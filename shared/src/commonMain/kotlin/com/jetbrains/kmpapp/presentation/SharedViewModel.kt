package com.jetbrains.kmpapp.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class Observador internal constructor(
    private val job: Job,
) {
    fun cancelar() {
        job.cancel()
    }
}

abstract class SharedViewModel : ViewModel() {
    fun clearForIos() {
        viewModelScope.cancel()
    }

    protected fun <T> observar(
        flow: StateFlow<T>,
        onChange: (T) -> Unit,
    ): Observador =
        Observador(
            viewModelScope.launch {
                flow.collect { onChange(it) }
            },
        )
}
