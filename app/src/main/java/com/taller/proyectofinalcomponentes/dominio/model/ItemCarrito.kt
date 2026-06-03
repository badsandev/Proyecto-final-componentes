package com.taller.proyectofinalcomponentes.dominio.model

data class ItemCarrito(
    val productoId: String = "",
    val nombre: String = "",
    val precio: Double = 0.0,
    var cantidad: Int = 1,
    val categoria: String = "",
    val imageUrl: String = "",
    val oldPrice: Double? = null
)