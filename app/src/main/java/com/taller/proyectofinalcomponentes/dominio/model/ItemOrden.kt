package com.taller.proyectofinalcomponentes.dominio.model

data class ItemOrden(
    val productoId: String = "",
    val nombre: String = "",
    val precio: Double = 0.0,
    val cantidad: Int = 1,
    val categoria: String = ""
)