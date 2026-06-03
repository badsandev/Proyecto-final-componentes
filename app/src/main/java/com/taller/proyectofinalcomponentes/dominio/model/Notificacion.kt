package com.taller.proyectofinalcomponentes.dominio.model

data class Notificacion(
    val id: String = "",
    val usuarioId: String = "",
    val titulo: String = "",
    val mensaje: String = "",
    val tipo: TipoNotificacion = TipoNotificacion.GENERAL,
    val leida: Boolean = false,
    val fecha: Long = System.currentTimeMillis()
)