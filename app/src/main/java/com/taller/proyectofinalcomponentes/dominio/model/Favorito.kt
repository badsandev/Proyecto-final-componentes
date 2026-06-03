package com.taller.proyectofinalcomponentes.dominio.model

data class Favorito(
    val id: String = "",
    val usuarioId: String = "",
    val productoId: String = "",
    val nombre: String = "",
    val precio: Double = 0.0,
    val oldPrice: Double? = null,
    val categoria: String = "",
    val rating: Double = 0.0
)