package com.taller.proyectofinalcomponentes.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.taller.proyectofinalcomponentes.dominio.model.EstadoOrden
import com.taller.proyectofinalcomponentes.dominio.model.Orden
import kotlinx.coroutines.tasks.await

class OrdenRepositoryImpl {

    private val firestore = FirebaseFirestore.getInstance()
    private val ordenes   = firestore.collection("ordenes")
    private val productos = firestore.collection("productos")

    // ─── Crear orden ──────────────────────────────────────────────────────
    suspend fun crearOrden(orden: Orden): Result<String> {
        return try {
            val doc = ordenes.document()
            val ordenConId = orden.copy(id = doc.id)
            doc.set(ordenConId).await()
            Result.success(doc.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─── Órdenes de un usuario ────────────────────────────────────────────
    suspend fun obtenerOrdenesUsuario(usuarioId: String): Result<List<Orden>> {
        return try {
            val snapshot = ordenes
                .whereEqualTo("usuarioId", usuarioId)
                .orderBy("fecha", Query.Direction.DESCENDING)
                .get().await()
            Result.success(snapshot.toObjects(Orden::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─── Todas las órdenes (solo admin) ───────────────────────────────────
    suspend fun obtenerTodasLasOrdenes(): Result<List<Orden>> {
        return try {
            val snapshot = ordenes
                .orderBy("fecha", Query.Direction.DESCENDING)
                .get().await()
            Result.success(snapshot.toObjects(Orden::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─── Actualizar estado + descontar stock si es ENTREGADO ──────────────
    suspend fun actualizarEstado(
        ordenId: String,
        nuevoEstado: EstadoOrden
    ): Result<Unit> {
        return try {
            // 1. Actualizar estado en Firestore
            ordenes.document(ordenId)
                .update("estado", nuevoEstado.name)
                .await()

            // 2. Si se marcó como ENTREGADO → descontar stock
            if (nuevoEstado == EstadoOrden.ENTREGADO) {
                descontarStock(ordenId)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─── Descontar stock de cada producto de la orden ─────────────────────
    private suspend fun descontarStock(ordenId: String) {
        try {
            // Obtener la orden con sus items
            val ordenDoc = ordenes.document(ordenId).get().await()
            val orden = ordenDoc.toObject(Orden::class.java) ?: return

            orden.items.forEach { item ->
                if (item.productoId.isBlank()) return@forEach

                // Buscar el producto directamente por su ID de documento
                val productoDoc = productos.document(item.productoId).get().await()

                if (productoDoc.exists()) {
                    val stockActual = productoDoc.getLong("stock")?.toInt() ?: 0
                    val nuevoStock  = maxOf(0, stockActual - item.cantidad)
                    productos.document(item.productoId)
                        .update("stock", nuevoStock)
                        .await()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}