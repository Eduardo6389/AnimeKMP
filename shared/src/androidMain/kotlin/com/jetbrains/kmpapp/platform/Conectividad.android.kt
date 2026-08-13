package com.jetbrains.kmpapp.platform

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

actual fun crearConectividad(contexto: Any?): Conectividad {
    val context = requireNotNull(contexto as? Context)
    return ConectividadAndroid(context)
}

private class ConectividadAndroid(
    context: Context,
) : Conectividad {
    private val manager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val _conectado = MutableStateFlow(estaConectado())
    override val conectado: StateFlow<Boolean> = _conectado.asStateFlow()

    private val callback =
        object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) = actualizar()

            override fun onLost(network: Network) = actualizar()

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities,
            ) = actualizar()
        }

    init {
        manager.registerDefaultNetworkCallback(callback)
    }

    private fun actualizar() {
        _conectado.value = estaConectado()
    }

    private fun estaConectado(): Boolean {
        val red = manager.activeNetwork ?: return false
        val capacidades = manager.getNetworkCapabilities(red) ?: return false
        return capacidades.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
