package com.taller.proyectofinalcomponentes.presentacion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taller.proyectofinalcomponentes.data.repository.NotificacionRepositoryImpl
import com.taller.proyectofinalcomponentes.dominio.model.Notificacion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NotificacionesViewModel : ViewModel() {

    private val repo = NotificacionRepositoryImpl()

    private val _notificaciones = MutableStateFlow<List<Notificacion>>(emptyList())
    val notificaciones: StateFlow<List<Notificacion>> = _notificaciones

    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando

    private var _usuarioId = ""

    fun cargarNotificaciones(usuarioId: String) {
        if (usuarioId.isBlank()) return
        _usuarioId = usuarioId
        viewModelScope.launch {
            _cargando.value = true
            repo.obtenerNotificaciones(usuarioId)
                .onSuccess { lista -> _notificaciones.value = lista }
                .onFailure { /* sin datos, lista vacía */ }
            _cargando.value = false
        }
    }

    fun marcarComoLeida(notificacionId: String) {
        viewModelScope.launch {
            repo.marcarComoLeida(notificacionId)
                .onSuccess {
                    _notificaciones.value = _notificaciones.value.map {
                        if (it.id == notificacionId) it.copy(leida = true) else it
                    }
                }
        }
    }

    fun marcarTodasComoLeidas() {
        viewModelScope.launch {
            repo.marcarTodasComoLeidas(_usuarioId)
                .onSuccess {
                    _notificaciones.value = _notificaciones.value.map { it.copy(leida = true) }
                }
        }
    }

    fun eliminarNotificacion(notificacionId: String) {
        viewModelScope.launch {
            repo.eliminarNotificacion(notificacionId)
                .onSuccess {
                    _notificaciones.value = _notificaciones.value
                        .filter { it.id != notificacionId }
                }
        }
    }
}