package com.taller.proyectofinalcomponentes.dominio.model

data class Orden(
    val id: String = "",
    val usuarioId: String = "",
    val usuarioEmail: String = "",
    val items: List<ItemOrden> = emptyList(),
    val direccion: String = "",
    val metodoPago: String = "",
    val subtotal: Double = 0.0,
    val envio: Double = 5.0,
    val total: Double = 0.0,
    val estado: EstadoOrden = EstadoOrden.PENDIENTE,
    val fecha: Long = System.currentTimeMillis()

)
