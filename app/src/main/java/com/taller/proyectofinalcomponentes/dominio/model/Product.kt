package com.taller.proyectofinalcomponentes.dominio.model

data class Product(
    val id: String = "",
    val name: String = "",
    val category: String = "",
    val price: Double = 0.0,
    val oldPrice: Double? = null,
    val rating: Double = 4.5,
    val description: String = "",
    val imageUrl: String = "",
    val stock: Int = 0,
    val activo: Boolean = true,
    val fechaCreacion: Long = System.currentTimeMillis()
)