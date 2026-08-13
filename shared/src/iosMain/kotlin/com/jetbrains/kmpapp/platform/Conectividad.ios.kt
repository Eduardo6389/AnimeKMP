package com.jetbrains.kmpapp.platform

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.Network.nw_path_get_status
import platform.Network.nw_path_monitor_create
import platform.Network.nw_path_monitor_set_queue
import platform.Network.nw_path_monitor_set_update_handler
import platform.Network.nw_path_monitor_start
import platform.Network.nw_path_status_satisfied
import platform.darwin.dispatch_get_main_queue

@OptIn(ExperimentalForeignApi::class)
actual fun crearConectividad(contexto: Any?): Conectividad = ConectividadIos()

@OptIn(ExperimentalForeignApi::class)
private class ConectividadIos : Conectividad {
    private val _conectado = MutableStateFlow(true)
    override val conectado: StateFlow<Boolean> = _conectado.asStateFlow()
    private val monitor = nw_path_monitor_create()

    init {
        nw_path_monitor_set_update_handler(monitor) { path ->
            _conectado.value =
                nw_path_get_status(path) == nw_path_status_satisfied
        }
        nw_path_monitor_set_queue(monitor, dispatch_get_main_queue())
        nw_path_monitor_start(monitor)
    }
}
