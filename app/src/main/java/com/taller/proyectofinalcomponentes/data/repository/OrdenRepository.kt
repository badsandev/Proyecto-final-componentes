package com.taller.proyectofinalcomponentes.data.repository

import com.taller.proyectofinalcomponentes.dominio.model.Orden

interface OrdenRepository {
    suspend fun crearOrden(orden: Orden): Result<String>
    suspend fun obtenerOrdenesUsuario(usuarioId: String): Result<List<Orden>>
    suspend fun obtenerTodasLasOrdenes(): Result<List<Orden>>
    suspend fun actualizarEstado(ordenId: String, nuevoEstado: String): Result<Unit>
    suspend fun obtenerOrdenPorId(ordenId: String): Result<Orden>
}