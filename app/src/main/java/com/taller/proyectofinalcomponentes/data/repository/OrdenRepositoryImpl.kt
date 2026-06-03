package com.taller.proyectofinalcomponentes.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.taller.proyectofinalcomponentes.dominio.model.EstadoOrden
import com.taller.proyectofinalcomponentes.dominio.model.Orden
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class OrdenRepositoryImpl {

    private val firestore = FirebaseFirestore.getInstance()
    private val ordenes = firestore.collection("ordenes")
    private val productos = firestore.collection("productos")

    suspend fun crearOrden(orden: Orden): Result<String> {
        return try {
            val doc = ordenes.document()
            val ordenConId = orden.copy(id = doc.id)
            doc.set(ordenConId).await()
            Result.success(doc.id)
        } catch (e: Exception) {
            Log.e("OrdenRepository", "Error al crear orden", e)
            Result.failure(e)
        }
    }

    fun obtenerOrdenesUsuarioFlow(usuarioId: String): Flow<List<Orden>> = callbackFlow {
        Log.d("OrdenRepository", "Buscando órdenes del usuarioId = $usuarioId")

        val listener = ordenes
            .whereEqualTo("usuarioId", usuarioId)
            .orderBy("fecha", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    Log.e("OrdenRepository", "Error en listener de órdenes del usuario", error)
                    close(error)
                    return@addSnapshotListener
                }

                val lista = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(Orden::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        Log.e("OrdenRepository", "Error convirtiendo documento ${doc.id}", e)
                        null
                    }
                } ?: emptyList()

                Log.d("OrdenRepository", "Órdenes encontradas para usuario $usuarioId: ${lista.size}")
                trySend(lista)
            }

        awaitClose {
            listener.remove()
            Log.d("OrdenRepository", "Listener de órdenes del usuario removido")
        }
    }

    fun obtenerTodasLasOrdenesFlow(): Flow<List<Orden>> = callbackFlow {
        Log.d("OrdenRepository", "Escuchando todas las órdenes")

        val listener = ordenes
            .orderBy("fecha", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    Log.e("OrdenRepository", "Error en listener de todas las órdenes", error)
                    close(error)
                    return@addSnapshotListener
                }

                val lista = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(Orden::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        Log.e("OrdenRepository", "Error convirtiendo documento ${doc.id}", e)
                        null
                    }
                } ?: emptyList()

                Log.d("OrdenRepository", "Total órdenes encontradas: ${lista.size}")
                trySend(lista)
            }

        awaitClose {
            listener.remove()
            Log.d("OrdenRepository", "Listener de todas las órdenes removido")
        }
    }

    suspend fun obtenerOrdenesUsuario(usuarioId: String): Result<List<Orden>> {
        return try {
            val snapshot = ordenes
                .whereEqualTo("usuarioId", usuarioId)
                .orderBy("fecha", Query.Direction.DESCENDING)
                .get()
                .await()

            val lista = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Orden::class.java)?.copy(id = doc.id)
            }

            Result.success(lista)
        } catch (e: Exception) {
            Log.e("OrdenRepository", "Error obteniendo órdenes del usuario", e)
            Result.failure(e)
        }
    }

    suspend fun obtenerTodasLasOrdenes(): Result<List<Orden>> {
        return try {
            val snapshot = ordenes
                .orderBy("fecha", Query.Direction.DESCENDING)
                .get()
                .await()

            val lista = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Orden::class.java)?.copy(id = doc.id)
            }

            Result.success(lista)
        } catch (e: Exception) {
            Log.e("OrdenRepository", "Error obteniendo todas las órdenes", e)
            Result.failure(e)
        }
    }

    suspend fun actualizarEstado(ordenId: String, nuevoEstado: EstadoOrden): Result<Unit> {
        return try {
            ordenes.document(ordenId)
                .update("estado", nuevoEstado)
                .await()

            if (nuevoEstado == EstadoOrden.ENTREGADO) {
                descontarStock(ordenId)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("OrdenRepository", "Error actualizando estado de orden $ordenId", e)
            Result.failure(e)
        }
    }

    private suspend fun descontarStock(ordenId: String) {
        try {
            val ordenDoc = ordenes.document(ordenId).get().await()
            val orden = ordenDoc.toObject(Orden::class.java) ?: return

            orden.items.forEach { item ->
                if (item.productoId.isBlank()) return@forEach

                val productoDoc = productos.document(item.productoId).get().await()
                if (productoDoc.exists()) {
                    val stockActual = productoDoc.getLong("stock")?.toInt() ?: 0
                    productos.document(item.productoId)
                        .update("stock", maxOf(0, stockActual - item.cantidad))
                        .await()
                }
            }
        } catch (e: Exception) {
            Log.e("OrdenRepository", "Error descontando stock", e)
        }
    }
}