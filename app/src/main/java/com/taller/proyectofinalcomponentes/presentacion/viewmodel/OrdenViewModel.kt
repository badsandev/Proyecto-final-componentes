package com.taller.proyectofinalcomponentes.presentacion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taller.proyectofinalcomponentes.data.repository.NotificacionRepositoryImpl
import com.taller.proyectofinalcomponentes.data.repository.OrdenRepositoryImpl
import com.taller.proyectofinalcomponentes.dominio.model.EstadoOrden
import com.taller.proyectofinalcomponentes.dominio.model.ItemCarrito
import com.taller.proyectofinalcomponentes.dominio.model.ItemOrden
import com.taller.proyectofinalcomponentes.dominio.model.Notificacion
import com.taller.proyectofinalcomponentes.dominio.model.Orden
import com.taller.proyectofinalcomponentes.dominio.model.TipoNotificacion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class OrdenViewModel : ViewModel() {

    private val repo = OrdenRepositoryImpl()
    private val notifRepo = NotificacionRepositoryImpl()

    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando

    private val _mensajeUI = MutableStateFlow<String?>(null)
    val mensajeUI: StateFlow<String?> = _mensajeUI

    private val _usuarioIdFlow = MutableStateFlow("")

    val ordenesUsuario: StateFlow<List<Orden>> = _usuarioIdFlow
        .flatMapLatest { uid ->
            if (uid.isBlank()) {
                flowOf(emptyList())
            } else {
                repo.obtenerOrdenesUsuarioFlow(uid)
                    .catch { e ->
                        _mensajeUI.value = "Error: ${e.message}"
                        emit(emptyList())
                    }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Órdenes para administrador
    private val _ordenesAdmin = MutableStateFlow<List<Orden>>(emptyList())
    val ordenesAdmin: StateFlow<List<Orden>> = _ordenesAdmin

    fun cargarOrdenesUsuario(usuarioId: String) {
        _usuarioIdFlow.value = usuarioId
    }

    fun cargarTodasLasOrdenes() {
        viewModelScope.launch {
            _cargando.value = true
            repo.obtenerTodasLasOrdenesFlow()
                .catch { e ->
                    _mensajeUI.value = "Error: ${e.message}"
                    _cargando.value = false
                    emit(emptyList())
                }
                .collect { lista ->
                    _ordenesAdmin.value = lista
                    _cargando.value = false
                }
        }
    }

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
                ItemOrden(
                    productoId = it.productoId,
                    nombre = it.nombre,
                    precio = it.precio,
                    cantidad = it.cantidad,
                    categoria = it.categoria
                )
            }

            val subtotal = items.sumOf { it.precio * it.cantidad }

            val orden = Orden(
                usuarioId = usuarioId,
                usuarioEmail = usuarioEmail,
                items = itemsOrden,
                direccion = direccion,
                metodoPago = metodoPago,
                subtotal = subtotal,
                envio = 5.0,
                total = subtotal + 5.0,
                estado = EstadoOrden.PENDIENTE
            )

            repo.crearOrden(orden)
                .onSuccess {
                    notifRepo.crearNotificacion(
                        Notificacion(
                            usuarioId = usuarioId,
                            titulo = "¡Orden confirmada!",
                            mensaje = "Tu pedido por $${"%.2f".format(orden.total)} fue recibido.",
                            tipo = TipoNotificacion.ORDEN,
                            leida = false
                        )
                    )
                    _mensajeUI.value = "✓ Orden creada correctamente"
                }
                .onFailure { e ->
                    _mensajeUI.value = "Error al crear orden: ${e.message}"
                }

            _cargando.value = false
        }
    }

    fun actualizarEstado(ordenId: String, nuevoEstado: EstadoOrden) {
        viewModelScope.launch {
            repo.actualizarEstado(ordenId, nuevoEstado)
                .onSuccess {
                    val orden = _ordenesAdmin.value.find { it.id == ordenId }

                    orden?.let {
                        val (titulo, mensaje) = mensajePorEstado(nuevoEstado)
                        notifRepo.crearNotificacion(
                            Notificacion(
                                usuarioId = it.usuarioId,
                                titulo = titulo,
                                mensaje = mensaje,
                                tipo = TipoNotificacion.ORDEN,
                                leida = false
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

    private fun mensajePorEstado(estado: EstadoOrden): Pair<String, String> = when (estado) {
        EstadoOrden.CONFIRMADO -> "Orden confirmada" to "Tu pedido fue confirmado."
        EstadoOrden.PREPARANDO -> "Preparando tu pedido" to "Tu pedido está siendo preparado."
        EstadoOrden.ENVIADO -> "¡Tu pedido va en camino!" to "Tu pedido fue enviado."
        EstadoOrden.ENTREGADO -> "¡Pedido entregado!" to "Tu pedido fue entregado. ¡Gracias!"
        EstadoOrden.CANCELADO -> "Orden cancelada" to "Tu orden fue cancelada."
        else -> "Actualización de orden" to "El estado de tu orden fue actualizado."
    }

    fun limpiarMensaje() {
        _mensajeUI.value = null
    }
}