package com.taller.proyectofinalcomponentes.presentacion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taller.proyectofinalcomponentes.data.repository.NotificacionRepositoryImpl
import com.taller.proyectofinalcomponentes.data.repository.OrdenRepositoryImpl
import com.taller.proyectofinalcomponentes.dominio.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class OrdenViewModel : ViewModel() {

    private val repo      = OrdenRepositoryImpl()
    private val notifRepo = NotificacionRepositoryImpl()

    private val _ordenes   = MutableStateFlow<List<Orden>>(emptyList())
    val ordenes: StateFlow<List<Orden>> = _ordenes

    private val _cargando  = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando

    private val _mensajeUI = MutableStateFlow<String?>(null)
    val mensajeUI: StateFlow<String?> = _mensajeUI

    fun crearOrden(
        usuarioId: String,
        usuarioEmail: String,
        items: List<ItemCarrito>,
        direccion: String,
        metodoPago: String
    ) {
        viewModelScope.launch {
            _cargando.value = true
            val itemsOrden = items.map {
                ItemOrden(it.productoId, it.nombre, it.precio, it.cantidad, it.categoria)
            }
            val subtotal = items.sumOf { it.precio * it.cantidad }
            val orden = Orden(
                usuarioId    = usuarioId,
                usuarioEmail = usuarioEmail,
                items        = itemsOrden,
                direccion    = direccion,
                metodoPago   = metodoPago,
                subtotal     = subtotal,
                envio        = 5.0,
                total        = subtotal + 5.0,
                estado       = EstadoOrden.PENDIENTE
            )
            repo.crearOrden(orden)
                .onSuccess {
                    notifRepo.crearNotificacion(
                        Notificacion(
                            usuarioId = usuarioId,
                            titulo    = "¡Orden confirmada!",
                            mensaje   = "Tu pedido por $${"%.2f".format(orden.total)} fue recibido.",
                            tipo      = TipoNotificacion.ORDEN,
                            leida     = false
                        )
                    )
                    _mensajeUI.value = "✓ Orden creada correctamente"
                    cargarOrdenesUsuario(usuarioId)
                }
                .onFailure { e ->
                    _mensajeUI.value = "Error al crear orden: ${e.message}"
                }
            _cargando.value = false
        }
    }

    fun cargarOrdenesUsuario(usuarioId: String) {
        if (usuarioId.isBlank()) return
        viewModelScope.launch {
            _cargando.value = true
            repo.obtenerOrdenesUsuario(usuarioId)
                .onSuccess { lista -> _ordenes.value = lista }
                .onFailure { e -> _mensajeUI.value = "Error: ${e.message}" }
            _cargando.value = false
        }
    }

    fun cargarTodasLasOrdenes() {
        viewModelScope.launch {
            _cargando.value = true
            repo.obtenerTodasLasOrdenes()
                .onSuccess { lista -> _ordenes.value = lista }
                .onFailure { e -> _mensajeUI.value = "Error: ${e.message}" }
            _cargando.value = false
        }
    }

    // ─── Actualizar estado + notificar usuario + descontar stock ──────────
    fun actualizarEstado(ordenId: String, nuevoEstado: EstadoOrden) {
        viewModelScope.launch {
            repo.actualizarEstado(ordenId, nuevoEstado)
                .onSuccess {
                    // Actualizar lista local
                    _ordenes.value = _ordenes.value.map {
                        if (it.id == ordenId) it.copy(estado = nuevoEstado) else it
                    }

                    // Enviar notificación al usuario de la orden
                    val orden = _ordenes.value.find { it.id == ordenId }
                    orden?.let {
                        val (titulo, mensaje) = mensajePorEstado(nuevoEstado)
                        notifRepo.crearNotificacion(
                            Notificacion(
                                usuarioId = it.usuarioId,
                                titulo    = titulo,
                                mensaje   = mensaje,
                                tipo      = TipoNotificacion.ORDEN,
                                leida     = false
                            )
                        )
                    }
                    _mensajeUI.value = "✓ Estado actualizado a ${nuevoEstado.name}"
                }
                .onFailure { e ->
                    _mensajeUI.value = "Error: ${e.message}"
                }
        }
    }

    // ─── Recargar después de cambio ───────────────────────────────────────
    fun recargarOrdenes() {
        viewModelScope.launch {
            repo.obtenerTodasLasOrdenes()
                .onSuccess { lista -> _ordenes.value = lista }
                .onFailure {}
        }
    }

    private fun mensajePorEstado(estado: EstadoOrden): Pair<String, String> = when (estado) {
        EstadoOrden.CONFIRMADO -> "Orden confirmada " to "Tu pedido fue confirmado y está siendo preparado."
        EstadoOrden.PREPARANDO -> "Preparando tu pedido " to "Tu pedido está siendo preparado."
        EstadoOrden.ENVIADO    -> "¡Tu pedido va en camino! " to "Tu pedido fue enviado y llegará pronto."
        EstadoOrden.ENTREGADO  -> "¡Pedido entregado! " to "Tu pedido fue entregado. ¡Gracias por tu compra!"
        EstadoOrden.CANCELADO  -> "Orden cancelada " to "Tu orden fue cancelada."
        else                   -> "Actualización de orden" to "El estado de tu orden fue actualizado."
    }

    fun limpiarMensaje() { _mensajeUI.value = null }
}