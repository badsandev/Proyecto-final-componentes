package com.taller.proyectofinalcomponentes.data.repository

import com.taller.proyectofinalcomponentes.dominio.model.Notificacion

interface NotificacionRepository {

    suspend fun obtenerNotificaciones(usuarioId: String): Result<List<Notificacion>>
    suspend fun marcarComoLeida(notificacionId: String): Result<Unit>
    suspend fun crearNotificacion(notificacion: Notificacion): Result<Unit>
    suspend fun eliminarNotificacion(notificacionId: String): Result<Unit>

}