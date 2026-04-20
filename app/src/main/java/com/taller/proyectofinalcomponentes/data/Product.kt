package com.taller.proyectofinalcomponentes.data

data class Product(
    val id: Int,
    val name: String,
    val category: String,
    val price: Double,
    val oldPrice: Double? = null,
    val rating: Double = 4.5,
    val description: String = "",
    val imageUrl: String = ""
)