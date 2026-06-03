package com.taller.proyectofinalcomponentes.data.repository

import com.taller.proyectofinalcomponentes.dominio.model.Product
import kotlinx.coroutines.flow.Flow

interface ProductoRepository {


    fun obtenerProductos(): Flow<List<Product>>
    fun obtenerProductosPorCategoria(categoria: String): Flow<List<Product>>
    suspend fun obtenerProductoPorId(id: String): Result<Product>
    suspend fun agregarProducto(producto: Product): Result<String>
    suspend fun actualizarProducto(producto: Product): Result<Unit>
    suspend fun eliminarProducto(id: String): Result<Unit>
    suspend fun actualizarStock(id: String, nuevoStock: Int): Result<Unit>

}